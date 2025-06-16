package com.ruangkerja.rest.repository;

import com.ruangkerja.rest.entity.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
