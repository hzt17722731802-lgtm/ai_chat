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
    private static final int MAX_CONTEXT_SIZE = 20;
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
        
        for (Message message : messages) {
            try {
                String json = serializeMessage(message);
                if (json != null) {
                    redisTemplate.opsForList().rightPush(key, json);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize message", e);
            }
        }
        
        redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);
        
        trimToMaxSize(key);
    }

    @Override
    public List<Message> get(String conversationId) {
        String key = CHAT_MEMORY_PREFIX + conversationId;
        
        List<String> jsonMessages = redisTemplate.opsForList().range(key, -MAX_CONTEXT_SIZE, -1);
        
        if (jsonMessages == null || jsonMessages.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return deserializeMessages(jsonMessages);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize messages", e);
        }
    }

    @Override
    public void clear(String conversationId) {
        String key = CHAT_MEMORY_PREFIX + conversationId;
        redisTemplate.delete(key);
    }

    private void trimToMaxSize(String key) {
        Long size = redisTemplate.opsForList().size(key);
        if (size != null && size > MAX_CONTEXT_SIZE) {
            redisTemplate.opsForList().trim(key, size - MAX_CONTEXT_SIZE, -1);
        }
    }

    private String serializeMessage(Message message) throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        if (message instanceof UserMessage) {
            map.put(TYPE_KEY, TYPE_USER);
        } else if (message instanceof AssistantMessage) {
            map.put(TYPE_KEY, TYPE_ASSISTANT);
        } else {
            return null;
        }
        map.put(CONTENT_KEY, message.getText());
        return objectMapper.writeValueAsString(map);
    }

    private List<Message> deserializeMessages(List<String> jsonMessages) throws JsonProcessingException {
        List<Message> messages = new ArrayList<>();
        
        for (String json : jsonMessages) {
            Map<String, String> map = objectMapper.readValue(
                json, 
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
            );
            
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
