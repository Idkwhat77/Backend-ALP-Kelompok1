package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    @Query("SELECT p FROM Portfolio p WHERE p.candidate.id = :candidateId ORDER BY p.createdAt DESC")
    List<Portfolio> findByCandidateIdOrderByCreatedAtDesc(@Param("candidateId") Long candidateId);
    
    @Query("SELECT p FROM Portfolio p WHERE p.candidate.id = :candidateId AND p.type = :type ORDER BY p.createdAt DESC")
    List<Portfolio> findByCandidateIdAndTypeOrderByCreatedAtDesc(@Param("candidateId") Long candidateId, @Param("type") String type);
    
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.candidate.id = :candidateId")
    Long countByCandidateId(@Param("candidateId") Long candidateId);
}