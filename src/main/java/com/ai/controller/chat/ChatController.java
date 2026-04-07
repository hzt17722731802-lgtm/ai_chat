package com.ai.controller.chat;

import com.ai.entity.vo.IntentRecognitionResult;
import com.ai.repository.ChatHistoryRepository;
import com.ai.repository.MySqlChatHistoryRepository;
import com.ai.service.IntentRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    private final MySqlChatHistoryRepository mySqlChatHistoryRepository;

    private final IntentRecognitionService intentRecognitionService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam String prompt, @RequestParam(required = false) String chatId){
        if (chatId == null || chatId.isBlank()) {
            chatId = java.util.UUID.randomUUID().toString();
        }

        String finalChatId = chatId;
        
        try {
            IntentRecognitionResult intentResult = intentRecognitionService.recognizeIntent(prompt);
            
            log.info("========== 前置意图识别结果 ==========");
            log.info("用户输入: {}", prompt);
            log.info("识别意图: {}", intentResult.getIntent());
            log.info("实体数量: {}", intentResult.getEntities() != null ? intentResult.getEntities().size() : 0);
            if (intentResult.getEntities() != null && !intentResult.getEntities().isEmpty()) {
                for (IntentRecognitionResult.Entity entity : intentResult.getEntities()) {
                    log.info("  实体 - 类型: {}, 值: {}", entity.getType(), entity.getValue());
                }
            }
            log.info("======================================");
        } catch (Exception e) {
            log.error("前置意图识别失败", e);
        }

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
