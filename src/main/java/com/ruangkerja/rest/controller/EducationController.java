package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.EducationRequest;
import com.ruangkerja.rest.entity.Education;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.repository.EducationRepository;
import com.ruangkerja.rest.repository.CandidateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/candidates/{candidateId}/education")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Education API", description = "API endpoints for education management")
public class EducationController {
    
    private static final Logger logger = LoggerFactory.getLogger(EducationController.class);
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String EDUCATION_KEY = "education";
    private static final String EDUCATIONS_KEY = "educations";
    
    private final EducationRepository educationRepository;
    private final CandidateRepository candidateRepository;
    
    @PostMapping
    @Operation(summary = "Create education record", description = "Create a new education record for a candidate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Education created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or candidate not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> createEducation(
            @PathVariable Long candidateId,
            @Valid @RequestBody EducationRequest request) {
        try {
            // Find candidate by ID
            Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Candidate candidate = candidateOptional.get();

            // Create new education record
            Education education = new Education();
            education.setCandidate(candidate);
            education.setInstitutionName(request.getInstitutionName().trim());
            education.setStartYear(request.getStartYear());
            education.setEndYear(request.getEndYear());
            education.setProfileImageUrl(request.getProfileImageUrl());
            education.setProfileImagePath(request.getProfileImagePath());
            education.setImageUploadDate(request.getImageUploadDate());

            Education savedEducation = educationRepository.save(education);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Education record created successfully");
            response.put(EDUCATION_KEY, createEducationResponse(savedEducation));
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            logger.error("Failed to create education record for candidate {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to create education record"));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get education by candidate ID", description = "Retrieve all education records for a specific candidate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Education records retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    public ResponseEntity<Map<String, Object>> getEducationByCandidate(@PathVariable Long candidateId) {
        try {
            // Verify candidate exists
            Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<Education> educations = educationRepository.findByCandidateId(candidateId);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(EDUCATIONS_KEY, educations.stream()
                    .map(this::createEducationResponse)
                    .toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve education records for candidate {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to retrieve education records"));
        }
    }

    @GetMapping("/{educationId}")
    @Operation(summary = "Get specific education record")
    public ResponseEntity<Map<String, Object>> getEducationById(
            @PathVariable Long candidateId,
            @PathVariable Long educationId) {
        try {
            Optional<Education> educationOptional = educationRepository.findById(educationId);
            
            if (educationOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Education education = educationOptional.get();
            
            // Verify education belongs to the specified candidate
            if (!education.getCandidate().getId().equals(candidateId)) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(EDUCATION_KEY, createEducationResponse(education));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve education record {}: {}", educationId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{educationId}")
    @Operation(summary = "Update education record")
    public ResponseEntity<Map<String, Object>> updateEducation(
            @PathVariable Long candidateId,
            @PathVariable Long educationId,
            @Valid @RequestBody EducationRequest request) {
        try {
            Optional<Education> educationOptional = educationRepository.findById(educationId);
            
            if (educationOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Education education = educationOptional.get();
            
            // Verify education belongs to the specified candidate
            if (!education.getCandidate().getId().equals(candidateId)) {
                return ResponseEntity.notFound().build();
            }

            // Update education fields
            education.setInstitutionName(request.getInstitutionName().trim());
            education.setStartYear(request.getStartYear());
            education.setEndYear(request.getEndYear());
            education.setProfileImageUrl(request.getProfileImageUrl());
            education.setProfileImagePath(request.getProfileImagePath());
            education.setImageUploadDate(request.getImageUploadDate());

            Education updatedEducation = educationRepository.save(education);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Education record updated successfully");
            response.put(EDUCATION_KEY, createEducationResponse(updatedEducation));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to update education record {}: {}", educationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to update education record"));
        }
    }

    @DeleteMapping("/{educationId}")
    @Operation(summary = "Delete education record")
    public ResponseEntity<Map<String, Object>> deleteEducation(
            @PathVariable Long candidateId,
            @PathVariable Long educationId) {
        try {
            Optional<Education> educationOptional = educationRepository.findById(educationId);
            
            if (educationOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Education education = educationOptional.get();
            
            // Verify education belongs to the specified candidate
            if (!education.getCandidate().getId().equals(candidateId)) {
                return ResponseEntity.notFound().build();
            }

            educationRepository.deleteById(educationId);

            return ResponseEntity.noContent().build(); // 204 for successful deletion
            
        } catch (Exception e) {
            logger.error("Failed to delete education record {}: {}", educationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to delete education record"));
        }
    }

    // Helper methods
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, message);
        return response;
    }

    private Map<String, Object> createEducationResponse(Education education) {
        Map<String, Object> educationResponse = new HashMap<>();
        educationResponse.put("id", education.getId());
        educationResponse.put("candidateId", education.getCandidate().getId());
        educationResponse.put("institutionName", education.getInstitutionName());
        educationResponse.put("startYear", education.getStartYear());
        educationResponse.put("endYear", education.getEndYear());
        educationResponse.put("profileImageUrl", education.getProfileImageUrl());
        educationResponse.put("profileImagePath", education.getProfileImagePath());
        educationResponse.put("imageUploadDate", education.getImageUploadDate());
        return educationResponse;
    }
}