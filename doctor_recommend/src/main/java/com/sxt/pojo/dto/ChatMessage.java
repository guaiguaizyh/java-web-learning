package com.sxt.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    /**
     * 消息类型：user(用户消息) 或 assistant(AI助手消息)
     */
    private String type;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息发送时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 创建用户消息
     * @param content 消息内容
     * @return 用户消息对象
     */
    public static ChatMessage userMessage(String content) {
        return new ChatMessage("user", content, LocalDateTime.now());
    }
    
    /**
     * 创建AI助手消息
     * @param content 消息内容
     * @return AI助手消息对象
     */
    public static ChatMessage assistantMessage(String content) {
        return new ChatMessage("assistant", content, LocalDateTime.now());
    }
} 