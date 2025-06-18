package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class JobRequest {
    
    @NotBlank(message = "Job title is required")
    private String title;
    
    private String type;
    private String province;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private String experience;
    
    @NotBlank(message = "Job description is required")
    private String description;
    
    private List<String> skills;
    private List<String> benefits;
    private LocalDate deadline;
    private String hiringProcess;
    private String applicationQuestions;
    
    // Add if using company authentication
    // private Long companyId;
}