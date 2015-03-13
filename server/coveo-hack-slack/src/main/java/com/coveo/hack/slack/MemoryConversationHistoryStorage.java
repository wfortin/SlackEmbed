package com.coveo.hack.slack;

import com.coveo.hack.slack.model.ChatMessage;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemoryConversationHistoryStorage implements ConversationHistoryStorage
{
    private Map<String, List<ChatMessage>> cache = new HashMap<>();

    @Override
    public void clear(String userId)
    {
        Optional.ofNullable(cache.get(userId)).ifPresent(List::clear);
    }

    @Override
    public void add(String userId,
                    ChatMessage message)
    {
        Optional.ofNullable(cache.putIfAbsent(userId, Lists.newArrayList(message))).ifPresent(l -> l.add(message));
    }

    @Override
    public List<ChatMessage> get(String userId)
    {
        return Optional.ofNullable(cache.get(userId)).orElse(new ArrayList<>());
    }
}
