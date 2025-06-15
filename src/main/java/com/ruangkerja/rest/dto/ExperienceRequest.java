package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceRequest {

    @NotBlank(message = "Experience name is required")
    @Size(min = 2, max = 100, message = "Experience name must be between 2 and 100 characters")
    private String experienceName;

    private Integer startYear;
    private Integer endYear;
    private String profileImageUrl;
    private String profileImagePath;
    private LocalDateTime imageUploadDate;
}