package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.SocialMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialMediaRepository extends JpaRepository<SocialMedia, Long> {
    
    @Query("SELECT sm FROM SocialMedia sm JOIN sm.candidates c WHERE c.id = :candidateId")
    List<SocialMedia> findByCandidateId(@Param("candidateId") Long candidateId);
    
    @Query("SELECT sm FROM SocialMedia sm WHERE sm.platform = :platform AND sm.url = :url")
    Optional<SocialMedia> findByPlatformAndUrl(@Param("platform") String platform, @Param("url") String url);
    
}