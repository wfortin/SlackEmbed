package com.ullink.slack.simpleslackapi;

import java.util.Collection;

public interface SlackSession
{

    Collection<SlackChannel> getChannels();

    Collection<SlackUser> getUsers();

    Collection<SlackBot> getBots();

    SlackChannel findChannelByName(String channelName);

    SlackChannel findChannelById(String channelId);

    SlackUser findUserById(String userId);

    SlackUser findUserByUserName(String userName);

    SlackUser findUserByEmail(String userMail);

    SlackBot findBotById(String botId);

    void connect();

    void archiveChannel(SlackChannel channel);

    SlackMessageHandle deleteMessage(String timeStamp, SlackChannel channel);

    SlackMessageHandle sendMessage(
            SlackChannel channel,
            String message,
            SlackAttachment attachment,
            String username,
            String iconURL);

    SlackChannel createChannel(String channelName);

    SlackMessageHandle updateMessage(String timeStamp, SlackChannel channel, String message);

    SlackMessageHandle sendMessageOverWebSocket(SlackChannel channel, String message, SlackAttachment attachment);

    void addMessageListener(SlackMessageListener listenerToAdd);

    void removeMessageListener(SlackMessageListener listenerToRemove);

}
