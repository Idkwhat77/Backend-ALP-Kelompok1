package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.ExperienceRequest;
import com.ruangkerja.rest.entity.Experience;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.repository.ExperienceRepository;
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
@RequestMapping("/api/candidates/{candidateId}/experiences")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Experience API", description = "API endpoints for experience management")
public class ExperienceController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExperienceController.class);
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String EXPERIENCE_KEY = "experience";
    private static final String EXPERIENCES_KEY = "experiences";
    
    private final ExperienceRepository experienceRepository;
    private final CandidateRepository candidateRepository;
    
    @PostMapping
    @Operation(summary = "Create experience record", description = "Create a new experience record for a candidate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Experience created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or candidate not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> createExperience(
            @PathVariable Long candidateId,
            @Valid @RequestBody ExperienceRequest request) {
        try {
            // Find candidate by ID
            Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Candidate candidate = candidateOptional.get();

            // Create new experience record
            Experience experience = new Experience();
            experience.setCandidate(candidate);
            experience.setExperienceName(request.getExperienceName().trim());
            experience.setStartYear(request.getStartYear());
            experience.setEndYear(request.getEndYear());
            experience.setProfileImageUrl(request.getProfileImageUrl());
            experience.setProfileImagePath(request.getProfileImagePath());
            experience.setImageUploadDate(request.getImageUploadDate());

            Experience savedExperience = experienceRepository.save(experience);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Experience record created successfully");
            response.put(EXPERIENCE_KEY, createExperienceResponse(savedExperience));
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            logger.error("Failed to create experience record for candidate {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to create experience record"));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get experience by candidate ID", description = "Retrieve all experience records for a specific candidate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Experience records retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    public ResponseEntity<Map<String, Object>> getExperienceByCandidate(@PathVariable Long candidateId) {
        try {
            // Verify candidate exists
            Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<Experience> experiences = experienceRepository.findByCandidateId(candidateId);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(EXPERIENCES_KEY, experiences.stream()
                    .map(this::createExperienceResponse)
                    .toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve experience records for candidate {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to retrieve experience records"));
        }
    }

    @GetMapping("/{experienceId}")
    @Operation(summary = "Get specific experience record")
    public ResponseEntity<Map<String, Object>> getExperienceById(
            @PathVariable Long candidateId,
            @PathVariable Long experienceId) {
        try {
            Optional<Experience> experienceOptional = experienceRepository.findById(experienceId);
            
            if (experienceOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Experience experience = experienceOptional.get();
            
            // Verify experience belongs to the specified candidate
            if (!experience.getCandidate().getId().equals(candidateId)) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(EXPERIENCE_KEY, createExperienceResponse(experience));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve experience record {}: {}", experienceId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{experienceId}")
    @Operation(summary = "Update experience record")
    public ResponseEntity<Map<String, Object>> updateExperience(
            @PathVariable Long candidateId,
            @PathVariable Long experienceId,
            @Valid @RequestBody ExperienceRequest request) {
        try {
            Optional<Experience> experienceOptional = experienceRepository.findById(experienceId);
            
            if (experienceOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Experience experience = experienceOptional.get();
            
            // Verify experience belongs to the specified candidate
            if (!experience.getCandidate().getId().equals(candidateId)) {
                return ResponseEntity.notFound().build();
            }

            // Update experience fields
            experience.setExperienceName(request.getExperienceName().trim());
            experience.setStartYear(request.getStartYear());
            experience.setEndYear(request.getEndYear());
            experience.setProfileImageUrl(request.getProfileImageUrl());
            experience.setProfileImagePath(request.getProfileImagePath());
            experience.setImageUploadDate(request.getImageUploadDate());

            Experience updatedExperience = experienceRepository.save(experience);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Experience record updated successfully");
            response.put(EXPERIENCE_KEY, createExperienceResponse(updatedExperience));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to update experience record {}: {}", experienceId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to update experience record"));
        }
    }

    @DeleteMapping("/{experienceId}")
    @Operation(summary = "Delete experience record")
    public ResponseEntity<Map<String, Object>> deleteExperience(
            @PathVariable Long candidateId,
            @PathVariable Long experienceId) {
        try {
            Optional<Experience> experienceOptional = experienceRepository.findById(experienceId);
            
            if (experienceOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Experience experience = experienceOptional.get();
            
            // Verify experience belongs to the specified candidate
            if (!experience.getCandidate().getId().equals(candidateId)) {
                return ResponseEntity.notFound().build();
            }

            experienceRepository.deleteById(experienceId);

            return ResponseEntity.noContent().build(); // 204 for successful deletion
            
        } catch (Exception e) {
            logger.error("Failed to delete experience record {}: {}", experienceId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to delete experience record"));
        }
    }

    // Helper methods
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, message);
        return response;
    }

    private Map<String, Object> createExperienceResponse(Experience experience) {
        Map<String, Object> experienceResponse = new HashMap<>();
        experienceResponse.put("id", experience.getId());
        experienceResponse.put("candidateId", experience.getCandidate().getId());
        experienceResponse.put("experienceName", experience.getExperienceName());
        experienceResponse.put("startYear", experience.getStartYear());
        experienceResponse.put("endYear", experience.getEndYear());
        experienceResponse.put("profileImageUrl", experience.getProfileImageUrl());
        experienceResponse.put("profileImagePath", experience.getProfileImagePath());
        experienceResponse.put("imageUploadDate", experience.getImageUploadDate());
        return experienceResponse;
    }
}