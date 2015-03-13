package com.coveo.hack.slack;

import com.coveo.hack.slack.model.ChatMessage;

import java.util.List;

public interface ConversationHistoryStorage
{
    void clear(String userId);
    void add(String userId, ChatMessage message);
    List<ChatMessage> get(String userId);
}
