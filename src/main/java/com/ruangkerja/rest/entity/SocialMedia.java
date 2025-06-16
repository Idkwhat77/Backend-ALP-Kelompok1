package com.ruangkerja.rest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "social_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Platform name is required")
    @Size(min = 2, max = 50, message = "Platform name must be between 2 and 50 characters")
    @Column(name = "platform", nullable = false)
    private String platform; // e.g., "LinkedIn", "Twitter", "GitHub", "Instagram"

    @NotBlank(message = "URL is required")
    @Size(max = 255, message = "URL must not exceed 255 characters")
    @Column(name = "url", nullable = false)
    private String url;

    @ManyToMany(mappedBy = "socialMedias", fetch = FetchType.LAZY)
    private List<Candidate> candidates = new ArrayList<>();
}