package cn.edu.zjweu.cs.shuzimali.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long conversationId;
    private Integer userId;
    private String role; // 'user' or 'assistant'
    private String content;
    private LocalDateTime createdAt;
}


