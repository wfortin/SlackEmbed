package com.coveo.hack.slack;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class MemoryUserStorage implements UserStorage
{
    private Set<UserInfo> users = new HashSet<>();

    @Override
    public Optional<UserInfo> getUserFromSession(String sessionKey)
    {
        return findUserBy(u -> u.getSession().equals(sessionKey));
    }

    @Override
    public Optional<UserInfo> getUserByName(String username)
    {
        return findUserBy(u -> u.getName().equals(username));
    }

    @Override
    public Optional<UserInfo> getUserByChannel(String channelName)
    {
        return findUserBy(u -> u.getChannelName().isPresent() && u.getChannelName().get().equals(channelName));
    }

    @Override
    public UserInfo saveUser(UserInfo userInfo)
    {
        users.add(userInfo);
        return userInfo;
    }

    private Optional<UserInfo> findUserBy(Predicate<UserInfo> predicate)
    {
        return users.stream().filter(predicate).findFirst();
    }
}
