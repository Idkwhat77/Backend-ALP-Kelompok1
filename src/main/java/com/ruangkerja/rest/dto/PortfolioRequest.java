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
public class PortfolioRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotBlank(message = "URL is required")
    @Pattern(
        regexp = "^https?://.+", 
        message = "URL must be a valid HTTP or HTTPS URL"
    )
    @Size(max = 255, message = "URL must not exceed 255 characters")
    private String url;
    
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type; // web, mobile, design, github, behance, etc.
    
    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl; // Optional thumbnail
    
    private Long candidateId;
}