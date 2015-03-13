package com.coveo.hack.slack;

import java.util.Optional;

public class UserInfo
{
    private String id;
    private String name;
    private String currentSession;
    private Optional<String> channelName;

    public UserInfo(String id,
                    String name,
                    String currentSession)
    {
        this.id = id;
        this.name = name;
        this.currentSession = currentSession;
        this.channelName = Optional.empty();
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UserInfo userInfo = (UserInfo) o;

        if (name != null ? !name.equals(userInfo.name) : userInfo.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "UserInfo{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}
