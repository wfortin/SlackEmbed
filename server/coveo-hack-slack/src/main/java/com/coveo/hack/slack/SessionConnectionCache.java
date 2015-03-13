package com.coveo.hack.slack;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.Optional;

public interface SessionConnectionCache
{
    Optional<SocketIOClient> getClientForSession(String sessionKey);

    void addClientForSession(String sessionKey, SocketIOClient socketIOClient);
}
