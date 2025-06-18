package com.ruangkerja.rest.service;

import com.ruangkerja.rest.entity.CompanyRating;
import com.ruangkerja.rest.repository.CompanyRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompanyRatingService {

    @Autowired
    private CompanyRatingRepository companyRatingRepository;

    public Page<CompanyRating> getCompanyRatings(Long companyId, Pageable pageable) {
        return companyRatingRepository.findByCompanyIdOrderByCreatedAtDesc(companyId, pageable);
    }

    public CompanyRating createRating(CompanyRating rating) {
        // Validate rating
        if (rating.getRating() < 1 || rating.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        return companyRatingRepository.save(rating);
    }

    public boolean hasUserRatedCompany(Long reviewerId, Long companyId) {
        return companyRatingRepository.existsByReviewerIdAndCompanyId(reviewerId, companyId);
    }

    public Map<String, Object> getRatingSummary(Long companyId) {
        List<CompanyRating> ratings = companyRatingRepository.findByCompanyId(companyId);
        
        Map<String, Object> summary = new HashMap<>();
        
        if (ratings.isEmpty()) {
            summary.put("averageRating", 0.0);
            summary.put("totalReviews", 0);
            summary.put("breakdown", new HashMap<Integer, Integer>());
            return summary;
        }

        // Calculate average rating
        double averageRating = ratings.stream()
                .mapToInt(CompanyRating::getRating)
                .average()
                .orElse(0.0);

        // Calculate rating breakdown
        Map<Integer, Integer> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            breakdown.put(i, 0);
        }

        for (CompanyRating rating : ratings) {
            int ratingValue = rating.getRating();
            breakdown.put(ratingValue, breakdown.get(ratingValue) + 1);
        }

        summary.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        summary.put("totalReviews", ratings.size());
        summary.put("breakdown", breakdown);

        return summary;
    }

    public List<CompanyRating> getTopRatings(Long companyId, int limit) {
        return companyRatingRepository.findTopRatingsByCompanyId(companyId, limit);
    }

    public void deleteRating(Long ratingId, Long reviewerId) {
        CompanyRating rating = companyRatingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Rating not found"));
        
        if (!rating.getReviewerId().equals(reviewerId)) {
            throw new RuntimeException("Unauthorized to delete this rating");
        }
        
        companyRatingRepository.delete(rating);
    }
}