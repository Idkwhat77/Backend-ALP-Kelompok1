package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.CompanyRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRatingRepository extends JpaRepository<CompanyRating, Long> {
    
    Page<CompanyRating> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);
    
    List<CompanyRating> findByCompanyId(Long companyId);
    
    boolean existsByReviewerIdAndCompanyId(Long reviewerId, Long companyId);
    
    @Query("SELECT cr FROM CompanyRating cr WHERE cr.companyId = :companyId ORDER BY cr.rating DESC, cr.createdAt DESC LIMIT :limit")
    List<CompanyRating> findTopRatingsByCompanyId(@Param("companyId") Long companyId, @Param("limit") int limit);
    
    @Query("SELECT AVG(cr.rating) FROM CompanyRating cr WHERE cr.companyId = :companyId")
    Double findAverageRatingByCompanyId(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(cr) FROM CompanyRating cr WHERE cr.companyId = :companyId")
    Long countByCompanyId(@Param("companyId") Long companyId);
}