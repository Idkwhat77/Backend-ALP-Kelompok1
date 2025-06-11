package com.ruangkerja.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    
    private boolean success;
    private String message;
    private String imageUrl;
    private String imagePath;
}
