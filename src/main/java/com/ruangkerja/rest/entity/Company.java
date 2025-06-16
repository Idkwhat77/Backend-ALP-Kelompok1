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

    @NotBlank(message = "Industry is required")
    @Column(nullable = false)
    private String industry;

    @NotNull(message = "Company size is required")
    @Column(nullable = false)
    private Integer companySize;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "image_upload_date")
    private LocalDateTime imageUploadDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description = "Please update your biodata to provide more information about yourself.";
}