package com.ai.controller.chat;

import com.ai.entity.IntentRecognitionResult;
import com.ai.repository.ChatHistoryRepository;
import com.ai.repository.MySqlChatHistoryRepository;
import com.ai.service.IntentRecognitionService;
import com.ai.service.KnowledgeGraphRagService;
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

    private final KnowledgeGraphRagService knowledgeGraphRagService;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam String prompt, @RequestParam(required = false) String chatId){
        if (chatId == null || chatId.isBlank()) {
            chatId = java.util.UUID.randomUUID().toString();
        }

        String finalChatId = chatId;
        
        IntentRecognitionResult intentResult = null;
        String ragContext = "";
        
        try {
            intentResult = intentRecognitionService.recognizeIntent(prompt);
            
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
            
            ragContext = knowledgeGraphRagService.buildRagContext(intentResult);
            
            if (!ragContext.isEmpty()) {
                log.info("========== RAG知识上下文 ==========");
                log.info("{}", ragContext);
                log.info("====================================");
            }
        } catch (Exception e) {
            log.error("前置意图识别或RAG上下文构建失败", e);
        }

        UserMessage userMessage = new UserMessage(prompt);
        mySqlChatHistoryRepository.saveMessage(finalChatId, "user", prompt);

        StringBuilder fullResponse = new StringBuilder();

        String enhancedPrompt = buildEnhancedPrompt(prompt, ragContext);

        return chatClient
                .prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, finalChatId))
                .user(enhancedPrompt)
                .stream()
                .content()
                .doOnNext(content -> fullResponse.append(content))
                .doOnComplete(() -> {
                    String aiResponse = fullResponse.toString();
                    mySqlChatHistoryRepository.saveMessage(finalChatId, "assistant", aiResponse);
                });
    }

    private String buildEnhancedPrompt(String originalPrompt, String ragContext) {
        if (ragContext == null || ragContext.isEmpty()) {
            return originalPrompt;
        }
        
        return String.format(
            "你是一个专业的医疗客服。请根据以下知识库信息回答用户的问题。\n\n" +
            "【知识库信息】\n%s\n\n" +
            "【用户问题】\n%s\n\n" +
            "请基于上述知识库信息，用专业、准确、易懂的语言回答用户问题。如果知识库中没有相关信息，请基于你的医学知识进行回答，并说明这是通用建议。",
            ragContext,
            originalPrompt
        );
    }
}
