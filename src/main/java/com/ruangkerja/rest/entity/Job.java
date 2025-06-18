package com.ruangkerja.rest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Job title is required")
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "province")
    private String province;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "salary_min")
    private Integer salaryMin;
    
    @Column(name = "salary_max")
    private Integer salaryMax;
    
    @Column(name = "experience")
    private String experience;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "skills")
    private String skills; // comma-separated
    
    @Column(name = "benefits")
    private String benefits; // comma-separated
    
    @Column(name = "deadline")
    private LocalDate deadline;
    
    @Column(name = "hiring_process")
    private String hiringProcess;
    
    @Column(name = "application_questions", columnDefinition = "TEXT")
    private String applicationQuestions;
    
    @Column(name = "status")
    private String status = "ACTIVE"; // ACTIVE, CLOSED, DRAFT
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Company relationship - REQUIRED
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Company company;
}