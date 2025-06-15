package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationRequest {
    
    @NotBlank(message = "Institution name is required")
    @Size(min = 2, max = 100, message = "Institution name must be between 2 and 100 characters")
    private String institutionName;

    private Integer startYear;
    private Integer endYear;
    private String profileImageUrl;
    private String profileImagePath;
    private LocalDateTime imageUploadDate;
}