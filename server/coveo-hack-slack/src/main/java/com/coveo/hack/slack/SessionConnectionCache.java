package com.coveo.hack.slack;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.Optional;

public interface SessionConnectionCache
{
    Optional<SocketIOClient> getClientForSession(String sessionKey);

    void addSession(SocketIOClient socketIOClient);
}
