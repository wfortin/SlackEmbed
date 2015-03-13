package com.coveo.hack.slack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStorage
{
    Optional<UserInfo> getUserBySession(String sessionKey);

    Optional<UserInfo> getUserByName(String username);

    Optional<UserInfo> getUserById(String id);

    UserInfo saveUser(UserInfo userInfo);

    Optional<UserInfo> getUserByChannel(String channelName);

    List<UserInfo> getAllUsers();

    void remove(UserInfo userInfo);
}
