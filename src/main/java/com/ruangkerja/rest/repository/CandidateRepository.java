package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
    
    List<Candidate> findByProvince(String province);    // Employee-related queries
    @Query("SELECT c FROM Candidate c WHERE c.employerCompany.id = :companyId AND c.isActiveEmployee = :isActive")
    List<Candidate> findByEmployerCompanyIdAndIsActiveEmployee(@Param("companyId") Long companyId, @Param("isActive") Boolean isActive);

    List<Candidate> findByEmployerCompanyId(Long companyId);

    List<Candidate> findByIsActiveEmployee(Boolean isActive);
}