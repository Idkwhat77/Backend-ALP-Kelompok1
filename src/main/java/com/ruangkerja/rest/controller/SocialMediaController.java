package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.SocialMediaRequest;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.entity.SocialMedia;
import com.ruangkerja.rest.repository.CandidateRepository;
import com.ruangkerja.rest.repository.SocialMediaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Social Media API", description = "API endpoints for managing candidate social media links")
public class SocialMediaController {

    private static final Logger logger = LoggerFactory.getLogger(SocialMediaController.class);
    
    private final SocialMediaRepository socialMediaRepository;
    private final CandidateRepository candidateRepository;

    @Operation(summary = "Get candidate's social media links", description = "Retrieves all social media links for a specific candidate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Social media links retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Candidate not found"),
        @ApiResponse(responseCode = "400", description = "Invalid candidate ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{candidateId}/socials")
    public ResponseEntity<Map<String, Object>> getCandidateSocials(
            @Parameter(description = "Candidate ID") @PathVariable Long candidateId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Fetching social media links for candidate ID: {}", candidateId);
            
            // Validate candidate ID
            if (candidateId == null || candidateId <= 0) {
                logger.warn("Invalid candidate ID provided: {}", candidateId);
                response.put("success", false);
                response.put("message", "Invalid candidate ID");
                response.put("socials", List.of());
                return ResponseEntity.status(400).body(response);
            }
            
            // Check if candidate exists
            if (!candidateRepository.existsById(candidateId)) {
                logger.warn("Candidate not found for ID: {}", candidateId);
                response.put("success", false);
                response.put("message", "Candidate not found");
                response.put("socials", List.of());
                return ResponseEntity.status(404).body(response);
            }
            
            // Get social media links using simpler approach
            List<SocialMedia> socials = socialMediaRepository.findByCandidateId(candidateId);
            logger.info("Found {} social media links for candidate ID: {}", socials.size(), candidateId);
            
            // Convert to simple DTOs to avoid circular reference
            List<Map<String, Object>> socialDTOs = socials.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("socials", socialDTOs);
            response.put("message", "Social media links retrieved successfully");
            response.put("count", socialDTOs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error fetching social media links for candidate ID: {}", candidateId, e);
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            response.put("socials", List.of());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Add social media link", description = "Adds a new social media link for a candidate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Social media link added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Candidate not found"),
        @ApiResponse(responseCode = "409", description = "Social media link already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/socials")
    @Transactional
    public ResponseEntity<Map<String, Object>> addCandidateSocial(
            @Valid @RequestBody SocialMediaRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Adding social media link for candidate ID: {}, platform: {}", 
                       request.getCandidateId(), request.getPlatform());
            
            // Validate request
            if (request.getCandidateId() == null || request.getCandidateId() <= 0) {
                response.put("success", false);
                response.put("message", "Invalid candidate ID");
                return ResponseEntity.status(400).body(response);
            }
            
            if (request.getPlatform() == null || request.getPlatform().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Platform is required");
                return ResponseEntity.status(400).body(response);
            }
            
            if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "URL is required");
                return ResponseEntity.status(400).body(response);
            }
            
            // Get candidate with error handling
            Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
            
            // Check for duplicates
            List<SocialMedia> existingSocials = socialMediaRepository.findByCandidateId(request.getCandidateId());
            boolean alreadyExists = existingSocials.stream()
                .anyMatch(sm -> sm.getPlatform().equalsIgnoreCase(request.getPlatform().trim()) && 
                              sm.getUrl().equals(request.getUrl().trim()));
            
            if (alreadyExists) {
                response.put("success", false);
                response.put("message", "This social media link already exists for this candidate");
                return ResponseEntity.status(409).body(response);
            }
            
            // Create and save social media
            SocialMedia socialMedia = new SocialMedia();
            socialMedia.setPlatform(request.getPlatform().trim());
            socialMedia.setUrl(request.getUrl().trim());
            
            // Save social media first
            socialMedia = socialMediaRepository.save(socialMedia);
            logger.info("Created social media entry with ID: {}", socialMedia.getId());
            
            // Add relationship
            candidate.getSocialMedias().add(socialMedia);
            candidateRepository.save(candidate);
            
            response.put("success", true);
            response.put("social", convertToDTO(socialMedia));
            response.put("message", "Social media link added successfully");
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            logger.error("Error adding social media link", e);
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Delete social media link", description = "Removes a social media link from a candidate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Social media link deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid social media ID"),
        @ApiResponse(responseCode = "404", description = "Social media link not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/socials/{socialId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteCandidateSocial(
            @Parameter(description = "Social Media ID") @PathVariable Long socialId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Deleting social media link ID: {}", socialId);
            
            // Validate social ID
            if (socialId == null || socialId <= 0) {
                response.put("success", false);
                response.put("message", "Invalid social media ID");
                return ResponseEntity.status(400).body(response);
            }
            
            SocialMedia socialMedia = socialMediaRepository.findById(socialId)
                .orElseThrow(() -> new RuntimeException("Social media link not found"));
            
            // Remove relationships first
            for (Candidate candidate : socialMedia.getCandidates()) {
                candidate.getSocialMedias().remove(socialMedia);
                candidateRepository.save(candidate);
            }
            
            // Delete the social media entry
            socialMediaRepository.delete(socialMedia);
            logger.info("Deleted social media link ID: {}", socialId);
            
            response.put("success", true);
            response.put("message", "Social media link deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting social media link ID: {}", socialId, e);
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Update social media link", description = "Updates an existing social media link")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Social media link updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Social media link not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/socials/{socialId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateCandidateSocial(
            @Parameter(description = "Social Media ID") @PathVariable Long socialId,
            @Valid @RequestBody SocialMediaRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Updating social media link ID: {}", socialId);
            
            // Validate inputs
            if (socialId == null || socialId <= 0) {
                response.put("success", false);
                response.put("message", "Invalid social media ID");
                return ResponseEntity.status(400).body(response);
            }
            
            if (request.getPlatform() == null || request.getPlatform().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Platform is required");
                return ResponseEntity.status(400).body(response);
            }
            
            if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "URL is required");
                return ResponseEntity.status(400).body(response);
            }
            
            SocialMedia socialMedia = socialMediaRepository.findById(socialId)
                .orElseThrow(() -> new RuntimeException("Social media link not found"));
            
            socialMedia.setPlatform(request.getPlatform().trim());
            socialMedia.setUrl(request.getUrl().trim());
            
            socialMedia = socialMediaRepository.save(socialMedia);
            logger.info("Updated social media link ID: {}", socialId);
            
            response.put("success", true);
            response.put("social", convertToDTO(socialMedia));
            response.put("message", "Social media link updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating social media link ID: {}", socialId, e);
            response.put("success", false);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Helper method to convert entity to DTO and avoid circular reference
    private Map<String, Object> convertToDTO(SocialMedia socialMedia) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", socialMedia.getId());
        dto.put("platform", socialMedia.getPlatform());
        dto.put("url", socialMedia.getUrl());
        return dto;
    }
}