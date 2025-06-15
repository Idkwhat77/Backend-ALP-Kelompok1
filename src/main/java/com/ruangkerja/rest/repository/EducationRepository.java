package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    
    List<Education> findByCandidateId(Long candidateId);
    
    void deleteByCandidateId(Long candidateId);
}