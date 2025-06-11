package com.ruangkerja.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String profileImageUrl;
    private LocalDateTime imageUploadDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
