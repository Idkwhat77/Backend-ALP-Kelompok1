package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    
    // Filtering methods for companies.html
    List<Company> findByProvince(String province);
    List<Company> findByCity(String city);
    List<Company> findByIndustry(String industry);
    List<Company> findBySizeCategory(String sizeCategory);
    List<Company> findByIsFeatured(Boolean isFeatured);
    
    // Combined filtering with search
    @Query("SELECT c FROM Company c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.industry) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.hq) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:province IS NULL OR :province = '' OR c.province = :province) AND " +
           "(:city IS NULL OR :city = '' OR c.city = :city) AND " +
           "(:industry IS NULL OR :industry = '' OR c.industry = :industry) AND " +
           "(:sizeCategory IS NULL OR :sizeCategory = '' OR c.sizeCategory = :sizeCategory)")
    Page<Company> findCompaniesWithFilters(
        @Param("search") String search,
        @Param("province") String province,
        @Param("city") String city,
        @Param("industry") String industry,
        @Param("sizeCategory") String sizeCategory,
        Pageable pageable
    );
    
    // Get companies by size range
    @Query("SELECT c FROM Company c WHERE c.companySize BETWEEN :minSize AND :maxSize")
    List<Company> findByCompanySizeBetween(@Param("minSize") Integer minSize, @Param("maxSize") Integer maxSize);
}
