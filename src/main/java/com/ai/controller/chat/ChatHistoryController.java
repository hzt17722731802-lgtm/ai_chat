package com.ai.controller.chat;

import com.ai.entity.ChatHistory;
import com.ai.entity.vo.MessageVO;
import com.ai.repository.ChatHistoryRepository;
import com.ai.repository.MySqlChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/ai/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryRepository chatHistoryRepository;

    private final ChatMemory chatMemory;

    private final MySqlChatHistoryRepository mySqlChatHistoryRepository;

    @GetMapping("/{type}")
    public List<String> getChatIds(@PathVariable String type){
        return chatHistoryRepository.getChatIds(type);
    }

    @GetMapping("/{type}/{chatId}")
    public List<MessageVO> getChatHistory(@PathVariable String type, @PathVariable String chatId){
        List<MessageVO> allMessages = new ArrayList<>();
        
        List<ChatHistory> mysqlHistories = mySqlChatHistoryRepository.getHistoryByChatId(chatId);
        for (ChatHistory history : mysqlHistories) {
            MessageVO vo = new MessageVO();
            vo.setRole(history.getRole());
            vo.setContent(history.getContent());
            vo.setCreateTime(history.getCreateTime());
            allMessages.add(vo);
        }
        
        List<Message> redisMessages = chatMemory.get(chatId);
        for (Message message : redisMessages) {
            String role;
            if (message instanceof org.springframework.ai.chat.messages.UserMessage) {
                role = "user";
            } else if (message instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                role = "assistant";
            } else {
                role = "unknown";
            }
            
            boolean exists = allMessages.stream()
                .anyMatch(m -> m.getRole().equals(role) && m.getContent().equals(message.getText()));
            
            if (!exists) {
                MessageVO vo = new MessageVO();
                vo.setRole(role);
                vo.setContent(message.getText());
                allMessages.add(vo);
            }
        }
        
        allMessages.sort(Comparator.comparing(MessageVO::getCreateTime));
        
        return allMessages;
    }
}
