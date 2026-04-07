package com.ai.config;


import com.ai.repository.MySqlChatHistoryRepository;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setOverflow(false);
        paginationInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
