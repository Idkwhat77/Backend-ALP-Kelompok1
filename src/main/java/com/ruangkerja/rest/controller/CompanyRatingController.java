package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.entity.CompanyRating;
import com.ruangkerja.rest.service.CompanyRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "*")
public class CompanyRatingController {

    @Autowired
    private CompanyRatingService companyRatingService;

    @GetMapping("/{companyId}/ratings")
    public ResponseEntity<Map<String, Object>> getCompanyRatings(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<CompanyRating> ratingsPage = companyRatingService.getCompanyRatings(companyId, pageable);
            
            Map<String, Object> summary = companyRatingService.getRatingSummary(companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ratings", ratingsPage.getContent());
            response.put("totalPages", ratingsPage.getTotalPages());
            response.put("totalElements", ratingsPage.getTotalElements());
            response.put("currentPage", page);
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching ratings: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/{companyId}/ratings")
    public ResponseEntity<Map<String, Object>> createRating(
            @PathVariable Long companyId,
            @RequestBody Map<String, Object> ratingData,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Extract user ID from auth token (implement according to your auth system)
            Long reviewerId = extractUserIdFromToken(authHeader);
            
            if (reviewerId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }

            // Check if user already rated this company
            if (companyRatingService.hasUserRatedCompany(reviewerId, companyId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "You have already rated this company");
                return ResponseEntity.status(400).body(errorResponse);
            }

            CompanyRating rating = new CompanyRating();
            rating.setCompanyId(companyId);
            rating.setReviewerId(reviewerId);
            rating.setRating((Integer) ratingData.get("rating"));
            rating.setReview((String) ratingData.get("review"));

            CompanyRating savedRating = companyRatingService.createRating(rating);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rating", savedRating);
            response.put("message", "Rating submitted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error creating rating: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{companyId}/ratings/summary")
    public ResponseEntity<Map<String, Object>> getRatingSummary(@PathVariable Long companyId) {
        try {
            Map<String, Object> summary = companyRatingService.getRatingSummary(companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error fetching rating summary: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private Long extractUserIdFromToken(String authHeader) {
        // Implement token parsing logic based on your authentication system
        // This is a placeholder - replace with your actual implementation
        try {
            String token = authHeader.replace("Bearer ", "");
            // Parse JWT token and extract user ID
            // Return the user ID from the token
            return null; // Replace with actual implementation
        } catch (Exception e) {
            return null;
        }
    }
}