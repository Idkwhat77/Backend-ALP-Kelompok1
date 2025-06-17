package com.ruangkerja.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageWebSocketDto {
    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderProfileImageUrl;
    private Long receiverId;
    private LocalDateTime createdAt;
    private String type; // "message", "typing", "read"
}