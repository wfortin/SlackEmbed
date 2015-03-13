package com.ullink.slack.simpleslackapi.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackGroupJoined;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackMessageListener;
import com.ullink.slack.simpleslackapi.SlackReply;
import com.ullink.slack.simpleslackapi.SlackSession;

class SlackWebSocketSessionImpl extends AbstractSlackSessionImpl implements SlackSession, MessageHandler.Whole<String>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SlackWebSocketSessionImpl.class);

    private static final String SLACK_HTTPS_AUTH_URL = "https://slack.com/api/rtm.start?token=";

    private Session websocketSession;
    private String authToken;
    private String proxyAddress;
    private int proxyPort = -1;
    HttpHost proxyHost;
    private long lastPingSent = 0;
    private volatile long lastPingAck = 0;

    private long messageId = 0;

    private long lastConnectionTime = -1;

    private boolean reconnectOnDisconnection;

    private Map<Long, SlackMessageHandleImpl> pendingMessageMap = new ConcurrentHashMap<Long, SlackMessageHandleImpl>();

    private Thread connectionMonitoringThread = null;

    SlackWebSocketSessionImpl(String authToken,
                              Proxy.Type proxyType,
                              String proxyAddress,
                              int proxyPort,
                              boolean reconnectOnDisconnection)
    {
        this.authToken = authToken;
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
        this.proxyHost = new HttpHost(proxyAddress, proxyPort);
        this.reconnectOnDisconnection = reconnectOnDisconnection;
    }

    SlackWebSocketSessionImpl(String authToken,
                              boolean reconnectOnDisconnection)
    {
        this.authToken = authToken;
        this.reconnectOnDisconnection = reconnectOnDisconnection;
    }

    @Override
    public void connect()
    {
        long currentTime = System.nanoTime();
        while (lastConnectionTime >= 0 && currentTime - lastConnectionTime < TimeUnit.SECONDS.toNanos(30)) {
            LOGGER.warn("Previous connection was made less than 30s ago, waiting 10s before trying to connect");
            try {
                Thread.sleep(10000);
                currentTime = System.nanoTime();
            } catch (InterruptedException e) {
                // TODO: handle this case
            }
        }
        LOGGER.info("connecting to slack");
        lastConnectionTime = currentTime;
        try {
            HttpClient httpClient = getHttpClient();
            HttpGet request = new HttpGet(SLACK_HTTPS_AUTH_URL + authToken);
            HttpResponse response = httpClient.execute(request);
            String jsonResponse = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
            SlackJSONSessionStatusParser sessionParser = new SlackJSONSessionStatusParser(jsonResponse);
            sessionParser.parse();
            users = sessionParser.getUsers();
            bots = sessionParser.getBots();
            channels = sessionParser.getChannels();
            LOGGER.info(users.size() + " users found on this session");
            LOGGER.info(bots.size() + " bots found on this session");
            LOGGER.info(channels.size() + " channels found on this session");

            String wssurl = sessionParser.getWebSocketURL();

            LOGGER.debug("retrieved websocket URL : " + wssurl);
            ClientManager client = ClientManager.createClient();
            client.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);
            if (proxyAddress != null) {
                client.getProperties().put(ClientProperties.PROXY_URI, "http://" + proxyAddress + ":" + proxyPort);
            }
            final MessageHandler handler = this;
            LOGGER.debug("initiating connection to websocket");
            websocketSession = client.connectToServer(new Endpoint()
            {
                @Override
                public void onOpen(Session session,
                                   EndpointConfig config)
                {
                    session.addMessageHandler(handler);
                }

            }, URI.create(wssurl));
            for (SlackMessageListener slackMessageListener : messageListeners) {
                slackMessageListener.onSessionLoad(this);
            }
            if (websocketSession != null) {
                LOGGER.debug("websocket connection established");
                LOGGER.info("slack session ready");
            }
            if (connectionMonitoringThread == null) {
                LOGGER.debug("starting connection monitoring");
                startConnectionMonitoring();
            }
        } catch (Exception e) {
            // TODO : improve exception handling
            e.printStackTrace();
        }

    }

    private void startConnectionMonitoring()
    {
        connectionMonitoringThread = new Thread()
        {
            @Override
            public void run()
            {
                LOGGER.debug("monitoring thread started");
                while (true) {
                    try {
                        if (lastPingSent != lastPingAck) {
                            // disconnection happened
                            LOGGER.warn("Connection lost...");
                            websocketSession.close();
                            lastPingSent = 0;
                            lastPingAck = 0;
                            if (reconnectOnDisconnection) {
                                connect();
                                continue;
                            }
                        } else {
                            lastPingSent = getNextMessageId();
                            LOGGER.debug("sending ping " + lastPingSent);
                            websocketSession.getBasicRemote().sendText("{\"type\":\"ping\",\"id\":" + lastPingSent
                                    + "}");
                        }
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        break;
                    } catch (IOException e) {
                        LOGGER.error("unexpected exception on monitoring thread ", e);
                    }
                }
                LOGGER.debug("monitoring thread stopped");
            }
        };
        connectionMonitoringThread.start();
    }

    @Override
    public SlackMessageHandle sendMessage(SlackChannel channel,
                                          String message,
                                          SlackAttachment attachment,
                                          String userName,
                                          String iconURL)
    {
        SlackMessageHandleImpl handle = new SlackMessageHandleImpl(getNextMessageId());
        HttpClient client = getHttpClient();
        HttpPost request = new HttpPost("https://slack.com/api/chat.postMessage");
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("token", authToken));
        nameValuePairList.add(new BasicNameValuePair("channel", channel.getId()));
        nameValuePairList.add(new BasicNameValuePair("as_user", "false"));
        nameValuePairList.add(new BasicNameValuePair("link_names", "1"));
        nameValuePairList.add(new BasicNameValuePair("text", message));
        if (iconURL != null) {
            nameValuePairList.add(new BasicNameValuePair("icon_url", iconURL));
        }
        nameValuePairList.add(new BasicNameValuePair("username", userName));
        if (attachment != null) {
            nameValuePairList.add(new BasicNameValuePair("attachments",
                                                         SlackJSONAttachmentFormatter.encodeAttachments(attachment)
                                                                                     .toString()));
        }
        try {
            request.setEntity(new UrlEncodedFormEntity(nameValuePairList, "UTF-8"));
            HttpResponse response = client.execute(request);
            String jsonResponse = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
            LOGGER.debug("PostMessage return: " + jsonResponse);
            SlackReplyImpl reply = SlackJSONReplyParser.decode(parseObject(jsonResponse));
            handle.setSlackReply(reply);
        } catch (Exception e) {
            // TODO : improve exception handling
            e.printStackTrace();
        }
        return handle;
    }

    @Override
    public SlackChannel createChannel(String channelName)
    {
        HttpClient client = getHttpClient();
        HttpPost request = new HttpPost("https://slack.com/api/channels.create");
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("token", authToken));
        nameValuePairList.add(new BasicNameValuePair("name", channelName));
        try {
            request.setEntity(new UrlEncodedFormEntity(nameValuePairList, "UTF-8"));
            HttpResponse response = client.execute(request);
            String jsonResponse = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
            LOGGER.debug("Create Channel return: " + jsonResponse);
            SlackChannelImpl channel = SlackJSONParsingUtils.buildSlackChannel((JSONObject) parseObject(jsonResponse).get("channel"), users);
            channels.put(channel.getId(), channel);
            return channel;
        } catch (Exception e) {
            // TODO : improve exception handling
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SlackMessageHandle deleteMessage(String timeStamp,
                                            SlackChannel channel)
    {
        SlackMessageHandleImpl handle = new SlackMessageHandleImpl(getNextMessageId());
        HttpClient client = getHttpClient();
        HttpPost request = new HttpPost("https://slack.com/api/chat.delete");
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("token", authToken));
        nameValuePairList.add(new BasicNameValuePair("channel", channel.getId()));
        nameValuePairList.add(new BasicNameValuePair("ts", timeStamp));
        try {
            request.setEntity(new UrlEncodedFormEntity(nameValuePairList, "UTF-8"));
            HttpResponse response = client.execute(request);
            String jsonResponse = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
            LOGGER.debug("PostMessage return: " + jsonResponse);
            SlackReplyImpl reply = SlackJSONReplyParser.decode(parseObject(jsonResponse));
            handle.setSlackReply(reply);
        } catch (Exception e) {
            // TODO : improve exception handling
            e.printStackTrace();
        }
        return handle;
    }

    @Override
    public SlackMessageHandle updateMessage(String timeStamp,
                                            SlackChannel channel,
                                            String message)
    {
        SlackMessageHandleImpl handle = new SlackMessageHandleImpl(getNextMessageId());
        HttpClient client = getHttpClient();
        HttpPost request = new HttpPost("https://slack.com/api/chat.update");
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        nameValuePairList.add(new BasicNameValuePair("token", authToken));
        nameValuePairList.add(new BasicNameValuePair("ts", timeStamp));
        nameValuePairList.add(new BasicNameValuePair("channel", channel.getId()));
        nameValuePairList.add(new BasicNameValuePair("text", message));
        try {
            request.setEntity(new UrlEncodedFormEntity(nameValuePairList, "UTF-8"));
            HttpResponse response = client.execute(request);
            String jsonResponse = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
            LOGGER.debug("PostMessage return: " + jsonResponse);
            SlackReplyImpl reply = SlackJSONReplyParser.decode(parseObject(jsonResponse));
            handle.setSlackReply(reply);
        } catch (Exception e) {
            // TODO : improve exception handling
            e.printStackTrace();
        }
        return handle;
    }

    private HttpClient getHttpClient()
    {
        HttpClient client = null;
        if (proxyHost != null) {
            client = HttpClientBuilder.create().setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost)).build();
        } else {
            client = HttpClientBuilder.create().build();
        }
        return client;
    }

    @Override
    public SlackMessageHandle sendMessageOverWebSocket(SlackChannel channel,
                                                       String message,
                                                       SlackAttachment attachment)
    {
        SlackMessageHandleImpl handle = new SlackMessageHandleImpl(getNextMessageId());
        try {
            JSONObject messageJSON = new JSONObject();
            messageJSON.put("type", "message");
            messageJSON.put("channel", channel.getId());
            messageJSON.put("text", message);
            if (attachment != null) {
                messageJSON.put("attachments", SlackJSONAttachmentFormatter.encodeAttachments(attachment));
            }
            websocketSession.getBasicRemote().sendText(messageJSON.toJSONString());
        } catch (Exception e) {
            // TODO : improve exception handling
            e.printStackTrace();
        }
        return handle;
    }

    private synchronized long getNextMessageId()
    {
        return messageId++;
    }

    @Override
    public void onMessage(String message)
    {
        LOGGER.debug("receiving from websocket " + message);
        if (message.contains("{\"type\":\"pong\",\"reply_to\"")) {
            int rightBracketIdx = message.indexOf('}');
            String toParse = message.substring(26, rightBracketIdx);
            lastPingAck = Integer.parseInt(toParse);
            LOGGER.debug("pong received " + lastPingAck);
        } else {
            JSONObject object = parseObject(message);

            String type = (String) object.get("type");
            if (type == null) {
                // that's a reply
                SlackReply slackReply = SlackJSONReplyParser.decode(object);
                SlackMessageHandleImpl handle = pendingMessageMap.get(slackReply.getReplyTo());
                handle.setSlackReply(slackReply);
                pendingMessageMap.remove(slackReply.getReplyTo());
            } else if ("message".equals(type)) {
                SlackMessage slackMessage = SlackJSONMessageParser.decode(this, object);
                if (slackMessage != null) {
                    for (SlackMessageListener slackMessageListener : messageListeners) {
                        slackMessageListener.onMessage(slackMessage);
                    }
                }
            } else if ("group_joined".equals(type)) {
                SlackGroupJoined groupJoined = parseGroupJoined(object);
                if (groupJoined != null) {
                    SlackChannel channel = groupJoined.getSlackChannel();
                    if (channel != null) {
                        channels.put(channel.getId(), channel);
                    }
                }
            }
        }
    }

    private SlackGroupJoined parseGroupJoined(JSONObject object)
    {
        JSONObject channel = (JSONObject) object.get("channel");
        SlackChannel slackChannel = SlackJSONParsingUtils.buildSlackChannel(channel, users);
        return new SlackGroupJoinedImpl(slackChannel);
    }

    private JSONObject parseObject(String json)
    {
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(json);
            return object;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
