package com.ruangkerja.rest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_ratings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // Add this line
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;
    
    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer rating;
    
    @Column(columnDefinition = "TEXT")
    private String review;
    
    @CreationTimestamp
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id", insertable = false, updatable = false)
    private User reviewer;
    
    // Constructors, getters, setters...
}