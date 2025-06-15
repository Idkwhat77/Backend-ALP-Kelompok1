package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Experience;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByCandidateId(Long candidateId);

    void deleteByCandidateId(Long candidateId);
}