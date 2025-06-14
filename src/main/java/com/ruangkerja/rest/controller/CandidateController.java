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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Candidate API", description = "API endpoints for candidate management")
public class CandidateController {

    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String CANDIDATE_KEY = "candidate";
    private static final String CANDIDATES_KEY = "candidates";

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    
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
            candidate.setJobType(request.getJobType().trim());
            candidate.setIndustry(request.getIndustry().trim());
            candidate.setEmploymentStatus(request.getEmploymentStatus().trim());

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

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get candidate by user ID", description = "Retrieve candidate information by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Candidate retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
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
        candidateResponse.put("jobType", candidate.getJobType());
        candidateResponse.put("industry", candidate.getIndustry());
        candidateResponse.put("employmentStatus", candidate.getEmploymentStatus());
        return candidateResponse;
    }
}