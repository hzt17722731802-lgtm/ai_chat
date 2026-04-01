package com.ai.controller;

import com.ai.repository.ChatHistoryRepository;
import com.ai.repository.MySqlChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    private final MySqlChatHistoryRepository mySqlChatHistoryRepository;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam String prompt, @RequestParam(required = false) String chatId){
        if (chatId == null || chatId.isBlank()) {
            chatId = java.util.UUID.randomUUID().toString();
        }

        String finalChatId = chatId;
        
        chatHistoryRepository.save("chat", finalChatId);

        UserMessage userMessage = new UserMessage(prompt);
        mySqlChatHistoryRepository.saveMessage(finalChatId, "user", prompt);

        StringBuilder fullResponse = new StringBuilder();

        return chatClient
                .prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalChatId))
                .user(prompt)
                .stream()
                .content()
                .doOnNext(content -> fullResponse.append(content))
                .doOnComplete(() -> {
                    String aiResponse = fullResponse.toString();
                    mySqlChatHistoryRepository.saveMessage(finalChatId, "assistant", aiResponse);
                });
    }
}
