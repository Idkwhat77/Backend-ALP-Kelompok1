package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.SkillRequest;
import com.ruangkerja.rest.entity.Skill;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.repository.SkillRepository;
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
@RequestMapping("/api/candidates/{candidateId}/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Skills API", description = "API endpoints for skills management")
public class SkillController {

    private static final Logger logger = LoggerFactory.getLogger(SkillController.class);
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String SKILL_KEY = "skill";
    private static final String SKILLS_KEY = "skills";

    private final SkillRepository skillRepository;
    private final CandidateRepository candidateRepository;
    
    @PostMapping
    @Operation(summary = "Add skill to candidate", description = "Add a new skill to a candidate or associate existing skill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Skill added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or candidate not found"),
            @ApiResponse(responseCode = "409", description = "Skill already exists for this candidate"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> addSkillToCandidate(
            @PathVariable Long candidateId,
            @Valid @RequestBody SkillRequest request) {
        try {
            // Find candidate by ID
            Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found"));
            }

            Candidate candidate = candidateOptional.get();

            // Check if skill already exists
            Optional<Skill> existingSkill = skillRepository.findByName(request.getName().trim());
            
            Skill skill;
            if (existingSkill.isPresent()) {
                skill = existingSkill.get();
                
                // Check if candidate already has this skill
                if (candidate.getSkill().contains(skill)) {
                    return ResponseEntity.status(409).body(createErrorResponse("Candidate already has this skill"));
                }
            } else {
                // Create new skill
                skill = new Skill();
                skill.setName(request.getName().trim());
                skill = skillRepository.save(skill);
            }

            // Add skill to candidate
            candidate.getSkill().add(skill);
            candidateRepository.save(candidate);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Skill added to candidate successfully");
            response.put(SKILL_KEY, createSkillResponse(skill));
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            logger.error("Failed to add skill to candidate {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to add skill"));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get candidate skills", description = "Retrieve all skills for a specific candidate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skills retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate not found")
    })
    public ResponseEntity<Map<String, Object>> getCandidateSkills(@PathVariable Long candidateId) {
        try {
            // Verify candidate exists
            Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found"));
            }

            Candidate candidate = candidateOptional.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(SKILLS_KEY, candidate.getSkill().stream()
                    .map(this::createSkillResponse)
                    .toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve skills for candidate {}: {}", candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to retrieve skills"));
        }
    }

    @DeleteMapping("/{skillId}")
    @Operation(summary = "Remove skill from candidate", description = "Remove a skill from a candidate (does not delete the skill itself)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skill removed from candidate successfully"),
            @ApiResponse(responseCode = "404", description = "Candidate or skill not found"),
            @ApiResponse(responseCode = "400", description = "Candidate does not have this skill")
    })
    public ResponseEntity<Map<String, Object>> removeSkillFromCandidate(
            @PathVariable Long candidateId,
            @PathVariable Long skillId) {
        try {
            // Find candidate
            Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);
            if (candidateOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate not found"));
            }

            // Find skill
            Optional<Skill> skillOptional = skillRepository.findById(skillId);
            if (skillOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Skill not found"));
            }

            Candidate candidate = candidateOptional.get();
            Skill skill = skillOptional.get();

            // Check if candidate has this skill
            if (!candidate.getSkill().contains(skill)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Candidate does not have this skill"));
            }

            // Remove skill from candidate
            candidate.getSkill().remove(skill);
            candidateRepository.save(candidate);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Skill removed from candidate successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to remove skill {} from candidate {}: {}", skillId, candidateId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to remove skill"));
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Get all available skills", description = "Retrieve all skills in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All skills retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getAllSkills() {
        try {
            List<Skill> allSkills = skillRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(SKILLS_KEY, allSkills.stream()
                    .map(this::createSkillResponse)
                    .toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve all skills: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to retrieve skills"));
        }
    }

    // Helper methods
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, message);
        return response;
    }

    private Map<String, Object> createSkillResponse(Skill skill) {
        Map<String, Object> skillResponse = new HashMap<>();
        skillResponse.put("id", skill.getId());
        skillResponse.put("name", skill.getName());
        return skillResponse;
    }
}