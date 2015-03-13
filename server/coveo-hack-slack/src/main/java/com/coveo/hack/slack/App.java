package com.coveo.hack.slack;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.coveo.hack.slack.model.ChatMessage;
import com.coveo.hack.slack.model.WelcomeMessage;
import com.coveo.hack.slack.model.ParsedSlackMessage;
import com.coveo.hack.slack.model.SenderInfo;
import com.coveo.hack.slack.model.WidgetMessage;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackMessageListener;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final String SLACKLINE_BOT_NAME = "Slackline";
    private static final String SLACKLINE_BOT_ICON_URL = "https://raw.githubusercontent.com/wfortin/SlackEmbed/master/client/img/logo.png";

    private Duration SESSION_DURATION = Duration.ofMinutes(1);

    private UserStorage userStorage;
    private UserNameStrategy userNameStrategy;
    private SlackUserIconStrategy userIconStrategy;
    private RegexSlackMessageParser messageParser;
    private SessionConnectionCache connectionCache;
    private ConversationHistoryStorage conversationHistoryStorage;
    private UserAgentStringParser userAgentParser;

    private SlackSession slack;
    private SocketIOServer webSocketServer;

    private SlackChannel getDefaultSupportChannel()
    {
        return slack.findChannelByName("support");
    }

    public static void main(String[] args) throws Exception
    {
        App app = new App();
        app.start();
    }

    public void start()
    {
        userStorage = new MemoryUserStorage();
        userNameStrategy = new RandomUserNameStrategy();
        userIconStrategy = new RoboHashSlackUserIconStrategy();
        messageParser = new RegexSlackMessageParser();
        connectionCache = new MemorySessionConnectionCache();
        conversationHistoryStorage = new MemoryConversationHistoryStorage();
        userAgentParser = UADetectorServiceFactory.getResourceModuleParser();

        initSlackClient();
        initWebSocketServer();
        Executors.newScheduledThreadPool(10).scheduleAtFixedRate(this::cleanUp, 0, 10, TimeUnit.SECONDS);
    }

    private void onSlackMessage(SlackMessage slackMessage)
    {
        logger.info("SlackMessage: {}", slackMessage);
        ParsedSlackMessage parsedSlackMessage = messageParser.parse(slackMessage);
        if (parsedSlackMessage.getOperation() == ParsedSlackMessage.Operation.CHAT) {
            Optional<UserInfo> userInfo = userStorage.getUserByName(parsedSlackMessage.getTargetUsername());
            userInfo.ifPresent(user -> sendMessageToUser(user, "Agent", parsedSlackMessage.getChatText(), null));
        } else if (parsedSlackMessage.getOperation() == ParsedSlackMessage.Operation.UNKNOWN
                && parsedSlackMessage.getOriginalSlackMessage().getRawSubType() == null) {
            // Perhaps we're in a dedicated chat room?
            // We ignore anything with a subtype as they're not chat messages. See https://api.slack.com/events/message
            // TODO support message changes.
            Optional<UserInfo> userInfo = userStorage.getUserByChannel(parsedSlackMessage.getChannelName());
            userInfo.ifPresent(user -> sendMessageToUser(user, "Agent", parsedSlackMessage.getChatText(), null));
        } else if (parsedSlackMessage.getOperation() == ParsedSlackMessage.Operation.TAKE) {
            Optional<UserInfo> userInfo = userStorage.getUserByName(parsedSlackMessage.getTargetUsername());
            userInfo.ifPresent(u -> {
                SlackChannel dedicatedChannel = slack.createChannel("support-" + u.getName());
                u.takeToChannel(dedicatedChannel.getName());
                userStorage.saveUser(u);
                slack.sendMessage(parsedSlackMessage.getOriginalSlackMessage().getChannel(),
                                  "Took user @" + u.getName() + " to room #" + dedicatedChannel.getName() + ".",
                                  null,
                                  SLACKLINE_BOT_NAME,
                                  SLACKLINE_BOT_ICON_URL);
            });
        }
    }

    private void sendMessageToUser(UserInfo userInfo,
                                   String fromUsername,
                                   String chatText,
                                   ZonedDateTime messageTimestamp)
    {
        Optional<SocketIOClient> client = connectionCache.getClientForSession(userInfo.getCurrentSession());

        ChatMessage message = new ChatMessage();
        message.setUsername(fromUsername);
        message.setContent(chatText);

        if (messageTimestamp == null) {
            // messageTimestamp is only set when we're replaying history. Add this new message to history
            message.setTimestamp(ZonedDateTime.now());
            conversationHistoryStorage.add(userInfo.getId(), message);
        } else {
            message.setTimestamp(messageTimestamp);
        }

        client.ifPresent(c -> {
            logger.info("Sending message '{}' to user {}", message, userInfo.getName());
            c.sendEvent("chat", message);
        });
    }

    private void onChatMessage(WidgetMessage<ChatMessage> chatMessage)
    {
        logger.info("WidgetMessage: {}", chatMessage);
        Optional<UserInfo> userInfo = userStorage.getUserBySession(chatMessage.getSenderInfo()
                                                                              .getSessionId()
                                                                              .toString());
        userInfo.ifPresent(u -> {
            chatMessage.getMessage().setUsername(u.getName());
            chatMessage.getMessage().setTimestamp(ZonedDateTime.now());
            conversationHistoryStorage.add(u.getId(), chatMessage.getMessage());
            slack.sendMessage(u.isTaken() ? slack.findChannelByName(u.getChannelName().get())
                                         : getDefaultSupportChannel(),
                              chatMessage.getMessage().getContent(),
                              null,
                              u.getName(),
                              userIconStrategy.iconUrlForUser(u.getName()));
        });
    }

    private void initSlackClient()
    {
        slack = SlackSessionFactory.createWebSocketSlackSession(System.getProperty("slack.token"));
        slack.connect();
        slack.addMessageListener(new SlackMessageListener()
        {
            @Override
            public void onSessionLoad(SlackSession session)
            {
                // dunno what that means
            }

            @Override
            public void onMessage(SlackMessage message)
            {
                onSlackMessage(message);
            }
        });
    }

    private void initWebSocketServer()
    {
        Configuration config = new Configuration();
        config.setHostname("192.168.68.62");
        config.setPort(8080);
        config.getSocketConfig().setReuseAddress(true);
        config.setJsonSupport(new JacksonJsonSupport(new JSR310Module()));

        webSocketServer = new SocketIOServer(config);
        webSocketServer.addConnectListener(client -> {
            logger.info("New session connected {}" + client.getSessionId());
            connectionCache.addSession(client);
        });

        webSocketServer.addDisconnectListener(client -> {
            Optional<UserInfo> userInfo = userStorage.getUserBySession(client.getSessionId().toString());
            userInfo.ifPresent(u -> {
                logger.info("User {} disconnected session {}", u, client.getSessionId());
                connectionCache.removeSession(client);
                u.setCurrentSession(null);
                userStorage.saveUser(u);
            });
        });

        webSocketServer.addEventListener("welcome", WelcomeMessage.class, (client,
                                                                           data,
                                                                           ackSender) -> {
            Optional<UserInfo> userInfo = userStorage.getUserById(data.getUserId());
            userInfo.ifPresent(u -> {
                logger.info("New session {} for returning user {}.", client.getSessionId(), u);
                u.setCurrentSession(client.getSessionId().toString());
                userStorage.saveUser(u);
                for (ChatMessage previousChat : conversationHistoryStorage.get(u.getId())) {
                    sendMessageToUser(u, "You", previousChat.getContent(), previousChat.getTimestamp());
                }
            });
            if (!userInfo.isPresent()) {
                UserInfo newUserInfo = new UserInfo(data.getUserId(),
                                                    userNameStrategy.generateUserName(),
                                                    client.getSessionId().toString());
                logger.info("New user {} with session {}.", newUserInfo, client.getSessionId());
                userStorage.saveUser(newUserInfo);
                sendWelcomeMessageToSlack(client, data, newUserInfo);
            }
        });

        webSocketServer.addEventListener("chat", ChatMessage.class, (client,
                                                                     data,
                                                                     ackSender) -> {
            logger.info("Message for session: " + client.getSessionId());
            onChatMessage(new WidgetMessage<>(extractInfo(client), data));
        });

        webSocketServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(webSocketServer::stop));
    }

    private void sendWelcomeMessageToSlack(SocketIOClient client,
                                           WelcomeMessage data,
                                           UserInfo newUserInfo)
    {
        SlackAttachment attachment = new SlackAttachment("New user @" + newUserInfo.getName(), "New user @"
                + newUserInfo.getName(), null, null);
        attachment.addField("User ID", newUserInfo.getId(), true);
        attachment.addField("IP", client.getRemoteAddress().toString().replace("/", ""), true);
        attachment.addField("Language", data.getLanguage(), true);
        attachment.addField("Timezone", data.getTimezone(), true);
        ReadableUserAgent userAgent = userAgentParser.parse(data.getUserAgent() == null ? "" : data.getUserAgent());
        attachment.addField("OS", userAgent.getOperatingSystem().getName(), true);
        attachment.addField("Browser", userAgent.getFamily().getName() + " "
                + userAgent.getVersionNumber().toVersionString(), true);
        attachment.addField("Current page", data.getCurrentPage(), false);

        slack.sendMessage(getDefaultSupportChannel(), null, attachment, SLACKLINE_BOT_NAME, SLACKLINE_BOT_ICON_URL);
    }

    private SenderInfo extractInfo(SocketIOClient client)
    {
        return new SenderInfo(client.getRemoteAddress().toString(), client.getSessionId());
    }

    private void cleanUp()
    {
        for (UserInfo userInfo : userStorage.getAllUsers()) {
            if (Duration.between(userInfo.getLastInteractionTimestamp(), ZonedDateTime.now())
                        .compareTo(SESSION_DURATION) > 0) {
                logger.info("User {} is idle since {}.", userInfo, userInfo.getLastInteractionTimestamp());
                userStorage.remove(userInfo);
                slack.sendMessage(getDefaultSupportChannel(),
                                  "User " + userInfo.getName() + " is idle since "
                                          + userInfo.getLastInteractionTimestamp() + ".",
                                  null,
                                  SLACKLINE_BOT_NAME,
                                  SLACKLINE_BOT_ICON_URL);
                if (userInfo.isTaken()) {
                    Optional<SlackChannel> channel = Optional.ofNullable(slack.findChannelByName(userInfo.getChannelName()
                                                                                                         .get()));
                    channel.ifPresent(c -> {
                        logger.info("Archiving channel {}.", c);
                        slack.archiveChannel(c);
                    });
                }
            }
        }
    }
}
