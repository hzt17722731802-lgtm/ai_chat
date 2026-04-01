package com.ai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {

    private String role;
    private String content;
    private LocalDateTime createTime;

    public MessageVO(Message message){
        switch (message.getMessageType()){
            case USER -> role = "user";
            case ASSISTANT -> role = "assistant";
            default -> role = "";
        }
        this.content = message.getText();
    }
}
