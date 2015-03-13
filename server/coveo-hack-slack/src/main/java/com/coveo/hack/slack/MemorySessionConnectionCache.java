package com.coveo.hack.slack;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemorySessionConnectionCache implements SessionConnectionCache
{
    Map<String, SocketIOClient> userIdToClientCache = new HashMap<>();

    @Override
    public Optional<SocketIOClient> getClientForSession(String sessionId)
    {
        return Optional.ofNullable(userIdToClientCache.get(sessionId));
    }

    @Override
    public void addSession(SocketIOClient socketIOClient)
    {
        userIdToClientCache.put(socketIOClient.getSessionId().toString(), socketIOClient);
    }
}
