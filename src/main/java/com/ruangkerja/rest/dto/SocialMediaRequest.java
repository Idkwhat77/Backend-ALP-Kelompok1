package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaRequest {
    
    @NotBlank(message = "Platform is required")
    @Size(min = 2, max = 50, message = "Platform name must be between 2 and 50 characters")
    private String platform;
    
    @NotBlank(message = "URL is required")
    @Pattern(
        regexp = "^https?://.+", 
        message = "URL must be a valid HTTP or HTTPS URL"
    )
    @Size(max = 255, message = "URL must not exceed 255 characters")
    private String url;
    
    private Long candidateId;
}