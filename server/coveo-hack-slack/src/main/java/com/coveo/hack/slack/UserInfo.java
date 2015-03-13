package com.coveo.hack.slack;

import java.util.Optional;

public class UserInfo
{
    private String name;
    private final String session;
    private Optional<String> channelName;

    public UserInfo(String name, String session)
    {
        this.name = name;
        this.session = session;
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

    public String getSession()
    {
        return session;
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
}
