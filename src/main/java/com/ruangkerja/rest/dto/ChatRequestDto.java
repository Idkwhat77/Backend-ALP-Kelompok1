package com.ruangkerja.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {
    private String message;
    private List<ChatMessageDto> conversationHistory;
}
