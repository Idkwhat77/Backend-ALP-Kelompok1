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
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        try {
            // Validate user authentication
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "X-User-Id header is required");
                return ResponseEntity.status(400).body(errorResponse);
            }

            // Check if user already rated this company
            if (companyRatingService.hasUserRatedCompany(userId, companyId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "You have already rated this company");
                return ResponseEntity.status(400).body(errorResponse);
            }

            CompanyRating rating = new CompanyRating();
            rating.setCompanyId(companyId);
            rating.setReviewerId(userId);
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

    @DeleteMapping("/{companyId}/ratings/{ratingId}")
    public ResponseEntity<?> deleteCompanyRating(
            @PathVariable Long companyId,
            @PathVariable Long ratingId,
            @RequestHeader("X-User-Id") Long userId) {
        
        try {
            // Verify the rating exists and belongs to the user
            CompanyRating rating = companyRatingService.getRatingById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
            
            if (!rating.getReviewerId().equals(userId)) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "You can only delete your own reviews"));
            }
            
            companyRatingService.deleteRating(rating);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Review deleted successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}