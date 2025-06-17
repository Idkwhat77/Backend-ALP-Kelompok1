package com.ruangkerja.rest.dto;

import com.ruangkerja.rest.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long conversationId;
    private User sender;        // Use User entity directly
    private User receiver;      // Use User entity directly
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor for basic message creation
    public MessageDto(Long conversationId, User sender, User receiver, String content) {
        this.conversationId = conversationId;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
}