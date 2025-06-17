package com.ruangkerja.rest.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_candidate_user"))
    private User user;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(name = "contact_email", nullable = false)
    private String email;

    @NotNull(message = "Birth date is required")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @Column(name = "province")
    private String province;

    @NotBlank(message = "Job type is required")
    @Column(name = "job_type", nullable = false)
    private String jobType;

    @NotBlank(message = "Industry is required")
    @Column(nullable = false)
    private String industry;

    @NotBlank(message = "Employment status is required")
    @Column(name = "employment_status", nullable = false)
    private String employmentStatus;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "image_upload_date")
    private LocalDateTime imageUploadDate;

    @Column(name = "biodata", columnDefinition = "TEXT")
    private String biodata = "Please update your biodata to provide more information about yourself.";

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Experience> experiences = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "candidate_hobbies",
            joinColumns = @JoinColumn(name = "candidate_id", foreignKey = @ForeignKey(name = "fk_candidate_hobbies_candidate")),
            inverseJoinColumns = @JoinColumn(name = "hobby_id", foreignKey = @ForeignKey(name = "fk_candidate_hobbies_hobby")))
    private List<Hobby> hobby = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "candidate_skills",
            joinColumns = @JoinColumn(name = "candidate_id", foreignKey = @ForeignKey(name = "fk_candidate_skills_candidate")),
            inverseJoinColumns = @JoinColumn(name = "skill_id", foreignKey = @ForeignKey(name = "fk_candidate_skills_skill")))
    private List<Skill> skill = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "candidate_social_media",
        joinColumns = @JoinColumn(name = "candidate_id"),
        inverseJoinColumns = @JoinColumn(name = "social_media_id")
    )
    @JsonManagedReference("candidate-socials")
    private List<SocialMedia> socialMedias = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("candidate-portfolio")
    private List<Portfolio> portfolioItems = new ArrayList<>();
}