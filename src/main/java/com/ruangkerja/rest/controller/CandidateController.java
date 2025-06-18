package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.CandidateFormRequest;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.entity.User;
import com.ruangkerja.rest.repository.CandidateRepository;
import com.ruangkerja.rest.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Candidate API", description = "API endpoints for candidate management")
public class CandidateController {

    private static final Logger logger = LoggerFactory.getLogger(CandidateController.class);
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String CANDIDATE_KEY = "candidate";
    private static final String CANDIDATES_KEY = "candidates";
    private static final String IMAGE_URL_KEY = "imageUrl";

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/images/}")
    private String uploadDir;

    @PostMapping("/upload-image/user/{userId}")
    @Operation(summary = "Upload candidate profile image by user ID", description = "Upload a profile image for the candidate associated with the specified user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or candidate not found"),
            @ApiResponse(responseCode = "500", description = "File upload failed")
    })
    public ResponseEntity<Map<String, Object>> uploadProfileImageByUserId(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile file) {
        
        try {
            // Find candidate by user ID
            Optional<Candidate> candidateOptional = candidateRepository.findByUserId(userId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found for this user"));
            }

            Candidate candidate = candidateOptional.get();

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a file to upload"));
            }

            // Check file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(createErrorResponse("File size exceeds 5MB limit"));
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !isValidImageType(contentType)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid file type. Only JPG, PNG, GIF, WebP are allowed"));
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid file name"));
            }
            String cleanedOriginalFilename = StringUtils.cleanPath(originalFilename);
            String fileExtension = getFileExtension(cleanedOriginalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "_" + userId + "." + fileExtension;

            // Delete old profile image if exists
            if (candidate.getProfileImagePath() != null) {
                deleteOldProfileImage(candidate.getProfileImagePath());
            }

            // Save file
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Update candidate profile image info
            String imageUrl = "/api/v1/images/" + uniqueFilename;
            candidate.setProfileImageUrl(imageUrl);
            candidate.setProfileImagePath(targetLocation.toString());
            candidate.setImageUploadDate(LocalDateTime.now());

            candidateRepository.save(candidate);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Profile image uploaded successfully");
            response.put(IMAGE_URL_KEY, imageUrl);
            response.put(CANDIDATE_KEY, createCandidateResponse(candidate));

            return ResponseEntity.ok(response);

        } catch (IOException ex) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to upload file: " + ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Image upload failed"));
        }
    }

    @DeleteMapping("/delete-image/user/{userId}")
    @Operation(summary = "Delete candidate profile image by user ID", description = "Delete the profile image for the candidate associated with the specified user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Candidate not found"),
            @ApiResponse(responseCode = "404", description = "No image to delete")
    })
    public ResponseEntity<Map<String, Object>> deleteProfileImageByUserId(@PathVariable Long userId) {
        try {
            // Find candidate by user ID
            Optional<Candidate> candidateOptional = candidateRepository.findByUserId(userId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found for this user"));
            }

            Candidate candidate = candidateOptional.get();

            if (candidate.getProfileImagePath() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("No profile image to delete"));
            }

            // Delete physical file
            deleteOldProfileImage(candidate.getProfileImagePath());

            // Clear candidate profile image info
            candidate.setProfileImageUrl(null);
            candidate.setProfileImagePath(null);
            candidate.setImageUploadDate(null);

            candidateRepository.save(candidate);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Profile image deleted successfully");
            response.put(CANDIDATE_KEY, createCandidateResponse(candidate));

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to delete image"));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get candidate by user ID", description = "Retrieve candidate information by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found for this user")
    })
    public ResponseEntity<Map<String, Object>> getCandidateByUserId(@PathVariable Long userId) {
        try {
            Optional<Candidate> candidateOptional = candidateRepository.findByUserId(userId);
            
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found for this user"));
            }

            Candidate candidate = candidateOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(CANDIDATE_KEY, createCandidateResponse(candidate));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to retrieve candidate"));
        }
    }
    
    @PostMapping("/create")
    @Operation(summary = "Create a new candidate", description = "Create a new candidate profile with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Candidate already exists")
    })
    public ResponseEntity<Map<String, Object>> createCandidate(
            @Valid @RequestBody CandidateFormRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            // Find the user by ID from header
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("User not found"));
            }

            User user = userOptional.get();

            // Create new candidate
            Candidate candidate = new Candidate();
            candidate.setUser(user);
            candidate.setFullName(request.getFullName().trim());
            candidate.setEmail(request.getEmail().trim());
            candidate.setBirthDate(request.getBirthDate());
            candidate.setCity(request.getCity().trim());
            candidate.setProvince(request.getProvince());
            candidate.setJobType(request.getJobType().trim());
            candidate.setIndustry(request.getIndustry().trim());
            candidate.setEmploymentStatus(request.getEmploymentStatus().trim());

            Candidate savedCandidate = candidateRepository.save(candidate);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Candidate created successfully");
            response.put(CANDIDATE_KEY, createCandidateResponse(savedCandidate));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to create candidate"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get candidate by ID", description = "Retrieve candidate information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    public ResponseEntity<Map<String, Object>> getCandidateById(@PathVariable Long id) {
        try {
            Optional<Candidate> candidateOptional = candidateRepository.findById(id);
            
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found"));
            }

            Candidate candidate = candidateOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(CANDIDATE_KEY, createCandidateResponse(candidate));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found"));
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Get all candidates", description = "Retrieve all candidate profiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidates retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getAllCandidates() {
        try {
            List<Candidate> candidates = candidateRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(CANDIDATES_KEY, candidates.stream()
                    .map(this::createCandidateResponse)
                    .toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to retrieve candidates"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update candidate", description = "Update candidate information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate updated successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Map<String, Object>> updateCandidate(@PathVariable Long id, @Valid @RequestBody CandidateFormRequest request) {
        try {
            Optional<Candidate> candidateOptional = candidateRepository.findById(id);
            
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found"));
            }

            Candidate candidate = candidateOptional.get();

            // Update candidate fields
            candidate.setFullName(request.getFullName().trim());
            candidate.setEmail(request.getEmail().trim());
            candidate.setBirthDate(request.getBirthDate());
            candidate.setCity(request.getCity().trim());
            candidate.setProvince(request.getProvince());
            candidate.setJobType(request.getJobType().trim());
            candidate.setIndustry(request.getIndustry().trim());
            candidate.setEmploymentStatus(request.getEmploymentStatus().trim());
            candidate.setBiodata(request.getBiodata().trim());

            Candidate updatedCandidate = candidateRepository.save(candidate);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Candidate updated successfully");
            response.put(CANDIDATE_KEY, createCandidateResponse(updatedCandidate));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to update candidate"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete candidate", description = "Delete candidate by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    public ResponseEntity<Map<String, Object>> deleteCandidate(@PathVariable Long id) {
        try {
            Optional<Candidate> candidateOptional = candidateRepository.findById(id);
            
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found"));
            }

            candidateRepository.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Candidate deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to delete candidate"));
        }
    }

    // Get unique provinces for filter dropdown
    @GetMapping("/provinces")
    public ResponseEntity<Map<String, Object>> getProvinces() {
        try {
            List<String> provinces = candidateRepository.findAll()
                .stream()
                .map(Candidate::getProvince)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put("provinces", provinces);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to fetch provinces: " + ex.getMessage()));
        }
    }

    // Get cities by province
    @GetMapping("/cities/{province}")
    public ResponseEntity<Map<String, Object>> getCitiesByProvince(@PathVariable String province) {
        try {
            List<String> cities = candidateRepository.findByProvince(province)
                .stream()
                .map(Candidate::getCity)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put("cities", cities);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to fetch cities: " + ex.getMessage()));
        }
    }

    // Helper methods
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/svg+xml");
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg"; // default extension
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void deleteOldProfileImage(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            logger.error("Failed to delete old profile image: {}", imagePath, ex);
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, message);
        return response;
    }

    private Map<String, Object> createCandidateResponse(Candidate candidate) {
        Map<String, Object> candidateResponse = new HashMap<>();
        candidateResponse.put("id", candidate.getId());
        candidateResponse.put("userId", candidate.getUser().getId());
        candidateResponse.put("fullName", candidate.getFullName());
        candidateResponse.put("email", candidate.getEmail());
        candidateResponse.put("birthDate", candidate.getBirthDate());
        candidateResponse.put("city", candidate.getCity());
        candidateResponse.put("province", candidate.getProvince());
        candidateResponse.put("jobType", candidate.getJobType());
        candidateResponse.put("industry", candidate.getIndustry());
        candidateResponse.put("employmentStatus", candidate.getEmploymentStatus());
        candidateResponse.put("biodata", candidate.getBiodata());
        candidateResponse.put("profileImageUrl", candidate.getProfileImageUrl());
        candidateResponse.put("profileImagePath", candidate.getProfileImagePath());
        candidateResponse.put("imageUploadDate", candidate.getImageUploadDate());
        
        // Employee information
        candidateResponse.put("position", candidate.getPosition());
        candidateResponse.put("department", candidate.getDepartment());
        candidateResponse.put("hireDate", candidate.getHireDate());
        candidateResponse.put("employeeId", candidate.getEmployeeId());
        candidateResponse.put("isActiveEmployee", candidate.getIsActiveEmployee());
        
        // Company information if employee
        if (candidate.getEmployerCompany() != null) {
            Map<String, Object> companyInfo = new HashMap<>();
            companyInfo.put("id", candidate.getEmployerCompany().getId());
            companyInfo.put("name", candidate.getEmployerCompany().getCompanyName());
            companyInfo.put("industry", candidate.getEmployerCompany().getIndustry());
            candidateResponse.put("employerCompany", companyInfo);
        }
        
        return candidateResponse;
    }
}