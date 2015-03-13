package com.coveo.hack.slack;

import java.util.Optional;
import java.util.UUID;

public interface UserStorage
{
    Optional<UserInfo> getUserFromSession(String sessionKey);

    Optional<UserInfo> getUserByName(String username);

    UserInfo saveUser(UserInfo userInfo);

    Optional<UserInfo> getUserByChannel(String channelName);
}
