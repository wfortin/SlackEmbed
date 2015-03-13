package com.coveo.hack.slack.model;

import com.ullink.slack.simpleslackapi.SlackMessage;

// TODO - Split into various message types dedicated for each type of event. AKA better OOP plz.
public class ParsedSlackMessage
{
    public enum Operation
    {
        UNKNOWN,
        CHAT,
        TAKE
    }

    private Operation operation;
    private SlackMessage originalSlackMessage;
    private String targetUsername;
    private String chatText;
    private String channelName;

    public Operation getOperation()
    {
        return operation;
    }

    public void setOperation(Operation operation)
    {
        this.operation = operation;
    }

    public SlackMessage getOriginalSlackMessage()
    {
        return originalSlackMessage;
    }

    public void setOriginalSlackMessage(SlackMessage originalSlackMessage)
    {
        this.originalSlackMessage = originalSlackMessage;
    }

    public String getChatText()
    {
        return chatText;
    }

    public void setChatText(String setChatText)
    {
        this.chatText = setChatText;
    }

    public String getTargetUsername()
    {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername)
    {
        this.targetUsername = targetUsername;
    }

    public String getChannelName()
    {
        return channelName;
    }

    public void setChannelName(String channelName)
    {
        this.channelName = channelName;
    }

}
