package com.ai.config;


import com.ai.repository.MySqlChatHistoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfiguration {

    @Bean
    public ChatClient chatClient(OpenAiChatModel model, ChatMemory chatMemory, MySqlChatHistoryRepository mySqlChatHistoryRepository) {
        return ChatClient
                .builder(model)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                ).build();
    }

    @Bean
    public ChatMemory chatMemory(RedisChatMemory redisChatMemory){
        return redisChatMemory;
    }

    @Bean
    public RedisChatMemory redisChatMemory(org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate,
                                           com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new RedisChatMemory(redisTemplate, objectMapper);
    }
}
