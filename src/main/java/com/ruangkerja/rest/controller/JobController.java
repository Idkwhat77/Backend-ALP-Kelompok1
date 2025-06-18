package com.ruangkerja.rest.controller;

import org.springframework.web.bind.annotation.*;

import com.ruangkerja.rest.dto.JobRequest;
import com.ruangkerja.rest.entity.Company;
import com.ruangkerja.rest.entity.Job;
import com.ruangkerja.rest.entity.User;
import com.ruangkerja.rest.repository.CompanyRepository;
import com.ruangkerja.rest.repository.JobRepository;
import com.ruangkerja.rest.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Job API", description = "API endpoints for managing job postings")
public class JobController {
    
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    
    @Operation(summary = "Create job posting", description = "Creates a new job posting (companies only)")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createJob(
            @Valid @RequestBody JobRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Company-Id", required = false) Long companyId) {
        
        try {
            // Validate user authentication
            if (userId == null) {
                return error("MissingUserId", "X-User-Id header is required");
            }
            
            // Validate user exists
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return error("UserNotFound", "User not found");
            }
            
            // Validate company exists and belongs to user
            Optional<Company> companyOptional = companyRepository.findByUserId(userId);
            if (companyOptional.isEmpty()) {
                return error("CompanyNotFound", "Company profile not found for this user");
            }
            
            Company company = companyOptional.get();
            
            // Additional validation if company ID is provided in header
            if (companyId != null && !company.getId().equals(companyId)) {
                return error("CompanyMismatch", "Company ID does not match user's company");
            }
            
            // Create job posting
            Job job = new Job();
            job.setTitle(request.getTitle());
            job.setType(request.getType());
            job.setProvince(request.getProvince());
            job.setCity(request.getCity());
            job.setSalaryMin(request.getSalaryMin());
            job.setSalaryMax(request.getSalaryMax());
            job.setExperience(request.getExperience());
            job.setDescription(request.getDescription());
            job.setSkills(request.getSkills() != null ? String.join(",", request.getSkills()) : null);
            job.setBenefits(request.getBenefits() != null ? String.join(",", request.getBenefits()) : null);
            job.setDeadline(request.getDeadline());
            job.setHiringProcess(request.getHiringProcess());
            job.setApplicationQuestions(request.getApplicationQuestions());
            job.setCompany(company);
            job.setCreatedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            job.setStatus("ACTIVE");
            
            Job savedJob = jobRepository.save(job);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job posted successfully");
            response.put("job", convertToDTO(savedJob));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Operation(summary = "Get all jobs", description = "Retrieves all active job postings with optional filters")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            
            Page<Job> jobs;
            
            // Apply filters based on parameters
            if (hasFilters(search, type, province, city)) {
                jobs = jobRepository.findActiveJobsWithFilters(search, type, province, city, pageable);
            } else {
                jobs = jobRepository.findActiveJobs(pageable);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobs", jobs.getContent().stream().map(this::convertToDTO).toList());
            response.put("totalPages", jobs.getTotalPages());
            response.put("totalElements", jobs.getTotalElements());
            response.put("currentPage", jobs.getNumber());
            response.put("pageSize", jobs.getSize());
            
            // Add filter info for debugging
            Map<String, Object> appliedFilters = new HashMap<>();
            appliedFilters.put("search", search);
            appliedFilters.put("type", type);
            appliedFilters.put("province", province);
            appliedFilters.put("city", city);
            response.put("appliedFilters", appliedFilters);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch jobs: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Operation(summary = "Get jobs by company", description = "Retrieves jobs posted by a specific company")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<Map<String, Object>> getJobsByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Job> jobs = jobRepository.findByCompanyIdAndStatus(companyId, "ACTIVE", pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("jobs", jobs.getContent().stream().map(this::convertToDTO).toList());
            response.put("totalPages", jobs.getTotalPages());
            response.put("totalElements", jobs.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch company jobs: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Operation(summary = "Get job by ID", description = "Retrieves a specific job posting by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getJobById(@PathVariable Long id) {
        try {
            Optional<Job> jobOptional = jobRepository.findById(id);
            
            if (jobOptional.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Job not found");
                return ResponseEntity.notFound().build();
            }
            
            Job job = jobOptional.get();
            
            // Check if job is still active
            if (!"ACTIVE".equals(job.getStatus())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Job is no longer available");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("job", convertToDetailedDTO(job));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @Operation(summary = "Delete job posting", description = "Deletes a job posting (company owner only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteJob(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        try {
            // Validate user authentication
            if (userId == null) {
                return error("MissingUserId", "X-User-Id header is required");
            }
            
            // Find the job
            Optional<Job> jobOptional = jobRepository.findById(id);
            if (jobOptional.isEmpty()) {
                return error("JobNotFound", "Job not found");
            }
            
            Job job = jobOptional.get();
            
            // Check if user owns the company that posted this job
            Optional<Company> companyOptional = companyRepository.findByUserId(userId);
            if (companyOptional.isEmpty()) {
                return error("CompanyNotFound", "Company profile not found");
            }
            
            Company company = companyOptional.get();
            if (!job.getCompany().getId().equals(company.getId())) {
                return error("Unauthorized", "You can only delete your own job postings");
            }
            
            // Delete the job
            jobRepository.delete(job);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Job deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to delete job: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private Map<String, Object> convertToDTO(Job job) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", job.getId());
        dto.put("title", job.getTitle());
        dto.put("type", job.getType());
        dto.put("province", job.getProvince());
        dto.put("city", job.getCity());
        dto.put("salaryMin", job.getSalaryMin());
        dto.put("salaryMax", job.getSalaryMax());
        dto.put("experience", job.getExperience());
        dto.put("description", job.getDescription());
        dto.put("skills", job.getSkills() != null ? job.getSkills().split(",") : new String[0]);
        dto.put("benefits", job.getBenefits() != null ? job.getBenefits().split(",") : new String[0]);
        dto.put("deadline", job.getDeadline());
        dto.put("hiringProcess", job.getHiringProcess());
        dto.put("applicationQuestions", job.getApplicationQuestions());
        dto.put("status", job.getStatus());
        dto.put("createdAt", job.getCreatedAt());
        dto.put("updatedAt", job.getUpdatedAt());
        
        // Add company information
        if (job.getCompany() != null) {
            Map<String, Object> companyInfo = new HashMap<>();
            companyInfo.put("id", job.getCompany().getId());
            companyInfo.put("name", job.getCompany().getCompanyName());
            companyInfo.put("industry", job.getCompany().getIndustry());
            companyInfo.put("profileImageUrl", job.getCompany().getProfileImageUrl());
            dto.put("company", companyInfo);
        }
        
        return dto;
    }
    
    private Map<String, Object> convertToDetailedDTO(Job job) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", job.getId());
        dto.put("title", job.getTitle());
        dto.put("type", job.getType());
        dto.put("province", job.getProvince());
        dto.put("city", job.getCity());
        dto.put("salaryMin", job.getSalaryMin());
        dto.put("salaryMax", job.getSalaryMax());
        dto.put("experience", job.getExperience());
        dto.put("description", job.getDescription());
        dto.put("skills", job.getSkills() != null ? Arrays.asList(job.getSkills().split(",")) : new ArrayList<>());
        dto.put("benefits", job.getBenefits() != null ? Arrays.asList(job.getBenefits().split(",")) : new ArrayList<>());
        dto.put("deadline", job.getDeadline());
        dto.put("hiringProcess", job.getHiringProcess());
        dto.put("applicationQuestions", job.getApplicationQuestions());
        dto.put("status", job.getStatus());
        dto.put("createdAt", job.getCreatedAt());
        dto.put("updatedAt", job.getUpdatedAt());
        
        // Add detailed company information
        if (job.getCompany() != null) {
            Map<String, Object> companyInfo = new HashMap<>();
            companyInfo.put("id", job.getCompany().getId());
            companyInfo.put("name", job.getCompany().getCompanyName());
            companyInfo.put("companyName", job.getCompany().getCompanyName());
            companyInfo.put("industry", job.getCompany().getIndustry());
            companyInfo.put("companySize", job.getCompany().getCompanySize());
            companyInfo.put("hq", job.getCompany().getHq());
            companyInfo.put("province", job.getCompany().getProvince());
            companyInfo.put("city", job.getCompany().getCity());
            companyInfo.put("description", job.getCompany().getDescription());
            companyInfo.put("foundationDate", job.getCompany().getFoundationDate());
            companyInfo.put("email", job.getCompany().getEmail());
            companyInfo.put("phoneNumber", job.getCompany().getPhoneNumber());
            companyInfo.put("websiteUrl", job.getCompany().getWebsiteUrl());
            companyInfo.put("profileImageUrl", job.getCompany().getProfileImageUrl());
            dto.put("company", companyInfo);
        }
        
        return dto;
    }
    
    private ResponseEntity<Map<String, Object>> error(String errorCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }

    private boolean hasFilters(String search, String type, String province, String city) {
        return (search != null && !search.trim().isEmpty()) ||
               (type != null && !type.trim().isEmpty()) ||
               (province != null && !province.trim().isEmpty()) ||
               (city != null && !city.trim().isEmpty());
    }
}