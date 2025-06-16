package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotNull(message = "Company size is required")
    private Integer companySize;

    private String profileImageUrl;
    private String profileImagePath;
    private String description;
}
