package com.ai.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ai.entity.ChatHistory;
import com.ai.mapper.ChatHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MySqlChatHistoryRepository implements ChatHistoryRepository {

    private final ChatHistoryMapper chatHistoryMapper;

    @Override
    public void save(String type, String chatId) {
        // MySQL 中保存历史记录，这里只是记录 chatId
        // 实际的消息内容在 ChatMemory 中保存
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getChatId, chatId);
        Long count = chatHistoryMapper.selectCount(wrapper);

    }

    @Override
    public List<String> getChatIds(String type) {
        return chatHistoryMapper.selectDistinctChatIds();
    }

    public void saveMessage(String chatId, String role, String content) {
        ChatHistory history = new ChatHistory();
        history.setId(java.util.UUID.randomUUID().toString());
        history.setChatId(chatId);
        history.setRole(role);
        history.setContent(content);
        history.setCreateTime(LocalDateTime.now());
        chatHistoryMapper.insert(history);
    }

    public List<ChatHistory> getHistoryByChatId(String chatId) {
        return chatHistoryMapper.selectByChatId(chatId);
    }
}
