package com.ruangkerja.rest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEnhanced {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Column(name = "email", nullable = false, unique = true, length = 150)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "role", length = 20, nullable = false)
    private String role = "USER";
    
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    @Column(name = "profile_image_path", length = 500)
    private String profileImagePath;
    
    @Column(name = "profile_image_filename", length = 255)
    private String profileImageFilename;
    
    @Column(name = "image_upload_date")
    private LocalDateTime imageUploadDate;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    @Column(name = "location", length = 100)
    private String location;
    
    @Column(name = "website", length = 255)
    private String website;
    
    @Column(name = "company", length = 100)
    private String company;
    
    @Column(name = "position", length = 100)
    private String position;
    
    @Column(name = "linkedin_profile", length = 255)
    private String linkedinProfile;
    
    @Column(name = "github_profile", length = 255)
    private String githubProfile;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isActive == null) {
            isActive = true;
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Utility methods for image handling
    public void setProfileImage(String filename, String filePath, String url) {
        this.profileImageFilename = filename;
        this.profileImagePath = filePath;
        this.profileImageUrl = url;
        this.imageUploadDate = LocalDateTime.now();
    }
    
    public void clearProfileImage() {
        this.profileImageFilename = null;
        this.profileImagePath = null;
        this.profileImageUrl = null;
        this.imageUploadDate = null;
    }
    
    public boolean hasProfileImage() {
        return profileImageUrl != null && !profileImageUrl.trim().isEmpty();
    }
}
