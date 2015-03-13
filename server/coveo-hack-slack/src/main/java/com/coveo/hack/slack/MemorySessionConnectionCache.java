package com.coveo.hack.slack;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemorySessionConnectionCache implements SessionConnectionCache
{
    Map<String, SocketIOClient> cache = new HashMap<>();

    @Override
    public Optional<SocketIOClient> getClientForSession(String sessionKey)
    {
        return Optional.ofNullable(cache.get(sessionKey));
    }

    @Override
    public void addClientForSession(String sessionKey, SocketIOClient socketIOClient) {
        cache.put(sessionKey, socketIOClient);
    }
}
