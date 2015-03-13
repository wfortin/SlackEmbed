package com.coveo.hack.slack;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.coveo.hack.slack.model.ChatMessage;
import com.coveo.hack.slack.model.ParsedSlackMessage;
import com.coveo.hack.slack.model.SenderInfo;
import com.coveo.hack.slack.model.WidgetMessage;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackMessageListener;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class App
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final String SLACKLINE_BOT_NAME = "Slackline";

    private UserStorage userStorage;
    private UserNameStrategy userNameStrategy;
    private SlackUserIconStrategy userIconStrategy;
    private RegexSlackMessageParser messageParser;
    private SessionConnectionCache connectionCache;

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

        initSlackClient();
        initWebSocketServer();

    }

    private void onSlackMessage(SlackMessage slackMessage)
    {
        // TODO
        logger.info("SlackMessage: {}", slackMessage);
        ParsedSlackMessage parsedSlackMessage = messageParser.parse(slackMessage);
        if (parsedSlackMessage.getOperation() == ParsedSlackMessage.Operation.CHAT) {
            Optional<UserInfo> userInfo = userStorage.getUserByName(parsedSlackMessage.getTargetUsername());
            userInfo.ifPresent(user -> sendMessageToUser(user, parsedSlackMessage.getChatText()));
        } else if (parsedSlackMessage.getOperation() == ParsedSlackMessage.Operation.UNKNOWN
                && parsedSlackMessage.getOriginalSlackMessage().getRawSubType() == null) {
            // Perhaps we're in a dedicated chat room?
            // We ignore anything with a subtype as they're not chat messages. See https://api.slack.com/events/message
            // TODO support message changes.
            Optional<UserInfo> userInfo = userStorage.getUserByChannel(parsedSlackMessage.getChannelName());
            userInfo.ifPresent(user -> sendMessageToUser(user, parsedSlackMessage.getChatText()));
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
                                  userIconStrategy.iconUrlForUser(SLACKLINE_BOT_NAME));
            });
        }
        // TODO - Handle take operation
    }

    private void sendMessageToUser(UserInfo userInfo,
                                   String chatText)
    {
        // This is where I'd send my message to the websocket client.. IF I HAD ONE
        logger.info("Sending message '{}' to user {}", chatText, userInfo.getName());
        Optional<SocketIOClient> client = connectionCache.getClientForSession(userInfo.getSession());

        client.ifPresent(c -> {
            logger.info("Found client, sending");
            ChatMessage message = new ChatMessage();
            message.setContent(chatText);
            c.sendEvent("chat", message);
        });
    }

    private void onChatMessage(WidgetMessage<ChatMessage> chatMessage)
    {
        logger.info("WidgetMessage: {}", chatMessage);
        UserInfo userInfo = getUserInfoOrCreate(chatMessage.getSenderInfo().getSessionId().toString());
        slack.sendMessage(userInfo.isTaken() ? slack.findChannelByName(userInfo.getChannelName().get())
                                            : getDefaultSupportChannel(),
                          chatMessage.getMessage().getContent(),
                          null,
                          userInfo.getName(),
                          userIconStrategy.iconUrlForUser(userInfo.getName()));
    }

    private void initSlackClient()
    {
        slack = SlackSessionFactory.createWebSocketSlackSession("xoxp-4024322923-4024322937-4028795068-98cf7c");
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

        webSocketServer = new SocketIOServer(config);
        webSocketServer.addConnectListener(client -> {
            logger.info("New session connected: " + client.getSessionId());
            connectionCache.addClientForSession(client.getSessionId().toString(), client);
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

    private SenderInfo extractInfo(SocketIOClient client)
    {
        return new SenderInfo(client.getRemoteAddress().toString(), client.getSessionId());
    }

    private UserInfo getUserInfoOrCreate(String sessionKey)
    {
        Optional<UserInfo> userInfo = userStorage.getUserFromSession(sessionKey);
        if (!userInfo.isPresent()) {
            UserInfo newUser = new UserInfo(userNameStrategy.generateUserName(), sessionKey);
            userStorage.saveUser(newUser);
            userInfo = Optional.of(newUser);
        }
        return userInfo.get();
    }

}
