package com.itheima.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_history")
public class ChatHistory {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String chatId;

    private String role;

    private String content;

    private LocalDateTime createTime;
}
