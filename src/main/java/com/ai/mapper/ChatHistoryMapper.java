package com.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ai.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    List<ChatHistory> selectByChatId(@Param("chatId") String chatId);

    List<String> selectDistinctChatIds();
}
