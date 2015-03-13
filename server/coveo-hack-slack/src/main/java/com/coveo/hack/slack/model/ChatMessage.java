package com.coveo.hack.slack.model;

import java.time.ZonedDateTime;

public class ChatMessage
{
    private String username;
    private String content;
    private ZonedDateTime timestamp;

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public ZonedDateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime dateTime)
    {
        this.timestamp = dateTime;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @Override
    public String toString()
    {
        return content + timestamp == null ? "" : " (at " + timestamp + ")";
    }
}
