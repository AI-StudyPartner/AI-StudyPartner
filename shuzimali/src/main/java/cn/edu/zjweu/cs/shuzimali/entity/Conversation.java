package cn.edu.zjweu.cs.shuzimali.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Conversation {
    private Long id;
    private Integer userId;
    private String title;
    private String lastMessage;
    private LocalDateTime updatedAt;
}


