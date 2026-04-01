package com.ai.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisChatMemory implements ChatMemory {

    private static final String CHAT_MEMORY_PREFIX = "chat:memory:";
    private static final long TTL_HOURS = 24;
    private static final String TYPE_KEY = "@type";
    private static final String TYPE_USER = "user";
    private static final String TYPE_ASSISTANT = "assistant";
    private static final String CONTENT_KEY = "content";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisChatMemory(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = CHAT_MEMORY_PREFIX + conversationId;
        List<Message> existingMessages = get(conversationId);
        existingMessages.addAll(messages);

        try {
            String json = serializeMessages(existingMessages);
            redisTemplate.opsForValue().set(key, json, TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize messages", e);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = CHAT_MEMORY_PREFIX + conversationId;
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            return new ArrayList<>();
        }

        try {
            return deserializeMessages(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize messages", e);
        }
    }

    @Override
    public void clear(String conversationId) {
        String key = CHAT_MEMORY_PREFIX + conversationId;
        redisTemplate.delete(key);
    }

    private String serializeMessages(List<Message> messages) throws JsonProcessingException {
        List<Map<String, String>> messageMaps = new ArrayList<>();
        for (Message message : messages) {
            Map<String, String> map = new HashMap<>();
            if (message instanceof UserMessage) {
                map.put(TYPE_KEY, TYPE_USER);
            } else if (message instanceof AssistantMessage) {
                map.put(TYPE_KEY, TYPE_ASSISTANT);
            } else {
                continue;
            }
            map.put(CONTENT_KEY, message.getText());
            messageMaps.add(map);
        }
        return objectMapper.writeValueAsString(messageMaps);
    }

    private List<Message> deserializeMessages(String json) throws JsonProcessingException {
        List<Map<String, String>> messageMaps = objectMapper.readValue(
            json, 
            new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, String>>>() {}
        );
        
        List<Message> messages = new ArrayList<>();
        for (Map<String, String> map : messageMaps) {
            String type = map.get(TYPE_KEY);
            String content = map.get(CONTENT_KEY);
            
            if (TYPE_USER.equals(type)) {
                messages.add(new UserMessage(content));
            } else if (TYPE_ASSISTANT.equals(type)) {
                messages.add(new AssistantMessage(content));
            }
        }
        return messages;
    }
}
