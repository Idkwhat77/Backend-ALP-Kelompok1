package com.ruangkerja.rest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "education")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Institution name is required")
    @Size(min = 2, max = 100, message = "Institution name must be between 2 and 100 characters")
    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @NotNull(message = "Candidate is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, foreignKey = @ForeignKey(name = "fk_education_candidate"))
    private Candidate candidate;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "image_upload_date")
    private LocalDateTime imageUploadDate;

}