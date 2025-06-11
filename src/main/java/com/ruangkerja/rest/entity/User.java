package com.ruangkerja.rest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
      @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "role")
    private String role = "USER";
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @Column(name = "profile_image_path")
    private String profileImagePath;
    
    @Column(name = "image_upload_date")
    private LocalDateTime imageUploadDate;
    
    @Column(name = "image_file_name")
    private String imageFileName;
    
    @Column(name = "image_file_size")
    private Long imageFileSize;
    
    @Column(name = "image_content_type")
    private String imageContentType;
    
    @Column(name = "bio", length = 1000)
    private String bio;
    
    @Column(name = "job_title")
    private String jobTitle;
    
    @Column(name = "company")
    private String company;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "linkedin_url")
    private String linkedinUrl;
    
    @Column(name = "github_url")
    private String githubUrl;
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @Column(name = "skills", length = 2000)
    private String skills; // JSON string or comma-separated
    
    @Column(name = "experience_level")
    private String experienceLevel; // ENTRY, MID, SENIOR, LEAD, EXECUTIVE
    
    @Column(name = "availability_status")
    private String availabilityStatus = "AVAILABLE"; // AVAILABLE, BUSY, NOT_LOOKING
    
    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
