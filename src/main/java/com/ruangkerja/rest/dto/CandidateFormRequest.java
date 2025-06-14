package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.Email;
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
public class CandidateFormRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @NotBlank(message = "Industry is required")
    private String industry;

    @NotBlank(message = "Employment status is required")
    private String employmentStatus;
}