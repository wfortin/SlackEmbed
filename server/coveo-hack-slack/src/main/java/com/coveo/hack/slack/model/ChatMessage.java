package com.coveo.hack.slack.model;

public class ChatMessage
{
    private String content;

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @Override public String toString()
    {
        return content;
    }
}
