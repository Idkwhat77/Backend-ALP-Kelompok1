package com.ruangkerja.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private boolean success;
    private String message;
    private String error;

    public ChatResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
