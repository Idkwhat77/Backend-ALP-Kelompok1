package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CompanyFormRequest {
    
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "Foundation date is required")
    private LocalDate foundationDate;

    @NotBlank(message = "HQ is required")
    private String hq;

    private String province;
    private String city;

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotNull(message = "Company size is required")
    @Min(value = 1, message = "Company size must be at least 1")
    private Integer companySize;

    private String description;
    private String websiteUrl;
    private String phoneNumber;
    private String companyType;
}
