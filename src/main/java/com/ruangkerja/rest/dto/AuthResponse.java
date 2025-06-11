package com.ruangkerja.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    
    private boolean success;
    private String message;
    private String token;
    private UserResponse user;
    
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public AuthResponse(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }
    
    public AuthResponse(boolean success, String message, UserResponse user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }
    
    public AuthResponse(boolean success, String message, String token, UserResponse user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }
}
