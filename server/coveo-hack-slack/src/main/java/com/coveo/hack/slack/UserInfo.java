package com.coveo.hack.slack;

import java.time.ZonedDateTime;
import java.util.Optional;

public class UserInfo
{
    private String id;
    private String name;
    private String currentSession;
    private Optional<String> channelName;
    private ZonedDateTime lastInteraction;

    public UserInfo(String id,
                    String name,
                    String currentSession)
    {
        this.id = id;
        this.name = name;
        this.currentSession = currentSession;
        this.channelName = Optional.empty();
        this.lastInteraction = ZonedDateTime.now();
    }

    public String getName()

    {
        return name;
    }

    public boolean isTaken()
    {
        return getChannelName().isPresent();
    }

    public Optional<String> getChannelName()
    {
        return channelName;
    }

    public void takeToChannel(String channelName)
    {
        this.channelName = Optional.of(channelName);
    }

    public String getCurrentSession()
    {
        return currentSession;
    }

    public String getId()
    {
        return id;
    }

    public void setCurrentSession(String currentSession)
    {
        this.currentSession = currentSession;
    }

    public ZonedDateTime getLastInteractionTimestamp()
    {
        return this.lastInteraction;
    }

    public void touchLastInteraction()
    {
        this.lastInteraction = ZonedDateTime.now();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UserInfo userInfo = (UserInfo) o;

        if (id != null ? !id.equals(userInfo.id) : userInfo.id != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "UserInfo{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}
