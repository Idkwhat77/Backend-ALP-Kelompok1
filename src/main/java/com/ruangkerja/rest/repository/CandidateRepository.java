package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
}