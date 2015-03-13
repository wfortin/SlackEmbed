package com.coveo.hack.slack.model;

import java.util.UUID;

public class SenderInfo
{
    private String ip;
    private UUID sessionId;

    public SenderInfo(String ip,
                      UUID sessionId)
    {
        this.ip = ip;
        this.sessionId = sessionId;
    }

    public String getIp()
    {
        return ip;
    }

    public UUID getSessionId()
    {
        return sessionId;
    }

    @Override
    public String toString()
    {
        return "SenderInfo{" + "ip='" + ip + '\'' + ", sessionId=" + sessionId + '}';
    }
}
