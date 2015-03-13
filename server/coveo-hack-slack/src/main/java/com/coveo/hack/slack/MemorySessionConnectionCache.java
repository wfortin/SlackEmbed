package com.coveo.hack.slack;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemorySessionConnectionCache implements SessionConnectionCache
{
    Map<String, SocketIOClient> cache = new HashMap<>();

    @Override
    public Optional<SocketIOClient> getClientForSession(String sessionId)
    {
        return Optional.ofNullable(cache.get(sessionId));
    }

    @Override
    public void addSession(SocketIOClient socketIOClient)
    {
        cache.put(socketIOClient.getSessionId().toString(), socketIOClient);
    }

    @Override public void removeSession(SocketIOClient client)
    {
        cache.remove(client.getSessionId().toString());
    }
}
