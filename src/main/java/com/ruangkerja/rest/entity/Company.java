package com.ruangkerja.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_company_user"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false)
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(name = "contact_email", nullable = false)
    private String email;

    @NotNull(message = "Birth date is required")
    @Column(name = "foundation_date", nullable = false)
    private LocalDate foundationDate;

    @NotBlank(message = "HQ is required")
    @Column(nullable = false)
    private String hq;

    // Enhanced location fields
    @Column(name = "province")
    private String province;

    @Column(name = "city")
    private String city;

    @NotBlank(message = "Industry is required")
    @Column(nullable = false)
    private String industry;

    @NotNull(message = "Company size is required")
    @Column(nullable = false)
    private Integer companySize;

    // Add company size category for easier filtering
    @Column(name = "size_category")
    private String sizeCategory; // startup, small, medium, large, enterprise

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "image_upload_date")
    private LocalDateTime imageUploadDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description = "Please update your company description to provide more information.";

    // Additional fields for better company profiles
    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "company_type")
    private String companyType; // Public, Private, Startup, etc.

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Automatically update the size category based on company size
    @PrePersist
    @PreUpdate
    private void updateSizeCategory() {
        if (companySize != null) {
            if (companySize <= 50) {
                this.sizeCategory = "startup";
            } else if (companySize <= 200) {
                this.sizeCategory = "small";
            } else if (companySize <= 1000) {
                this.sizeCategory = "medium";
            } else if (companySize <= 5000) {
                this.sizeCategory = "large";
            } else {
                this.sizeCategory = "enterprise";
            }
        }
        this.updatedAt = LocalDateTime.now();
    }
}