package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' ORDER BY j.createdAt DESC")
    Page<Job> findActiveJobs(Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Job> findActiveJobsBySearch(@Param("search") String search, Pageable pageable);
    
    // Combined filter query
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(j.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(j.skills) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:type IS NULL OR :type = '' OR LOWER(j.type) = LOWER(:type)) AND " +
           "(:province IS NULL OR :province = '' OR LOWER(j.province) = LOWER(:province)) AND " +
           "(:city IS NULL OR :city = '' OR LOWER(j.city) = LOWER(:city)) " +
           "ORDER BY j.createdAt DESC")
    Page<Job> findActiveJobsWithFilters(
        @Param("search") String search,
        @Param("type") String type,
        @Param("province") String province,
        @Param("city") String city,
        Pageable pageable
    );
    
    // Existing methods
    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId AND j.status = :status ORDER BY j.createdAt DESC")
    Page<Job> findByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") String status, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId ORDER BY j.createdAt DESC")
    Page<Job> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND j.type = :type ORDER BY j.createdAt DESC")
    Page<Job> findActiveJobsByType(@Param("type") String type, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND j.province = :province ORDER BY j.createdAt DESC")
    Page<Job> findActiveJobsByProvince(@Param("province") String province, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND j.city = :city ORDER BY j.createdAt DESC")
    Page<Job> findActiveJobsByCity(@Param("city") String city, Pageable pageable);
}