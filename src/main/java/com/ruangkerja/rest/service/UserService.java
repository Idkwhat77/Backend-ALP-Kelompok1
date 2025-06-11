package com.ruangkerja.rest.service;

import com.ruangkerja.rest.dto.*;
import com.ruangkerja.rest.entity.User;
import com.ruangkerja.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Value("${app.upload.dir:uploads/images/}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Get user profile by ID
     */
    public Optional<UserResponse> getUserProfile(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getProfileImageUrl(),
                user.getImageUploadDate(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            );
            return Optional.of(userResponse);
        }
        
        return Optional.empty();
    }
    
    /**
     * Update user profile
     */
    public AuthResponse updateProfile(Long userId, UpdateProfileRequest request) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            
            if (userOptional.isEmpty()) {
                return new AuthResponse(false, "User not found");
            }
            
            User user = userOptional.get();
            
            // Update fields if provided
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                user.setName(request.getName().trim());
            }
            
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                // Check if email is already taken by another user
                if (userRepository.existsByEmail(request.getEmail()) && 
                    !user.getEmail().equals(request.getEmail())) {
                    return new AuthResponse(false, "Email is already taken");
                }
                user.setEmail(request.getEmail().trim());
            }
            
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone().trim());
            }
            
            User savedUser = userRepository.save(user);
            
            UserResponse userResponse = new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getRole(),
                savedUser.getProfileImageUrl(),
                savedUser.getImageUploadDate(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt()
            );
            
            return new AuthResponse(true, "Profile updated successfully", userResponse);
            
        } catch (Exception e) {
            return new AuthResponse(false, "Failed to update profile: " + e.getMessage());
        }
    }
    
    /**
     * Upload profile image
     */
    public ImageUploadResponse uploadProfileImage(Long userId, MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return new ImageUploadResponse(false, "File is empty", null, null);
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return new ImageUploadResponse(false, "File must be an image", null, null);
            }
            
            // Check file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                return new ImageUploadResponse(false, "File size must be less than 5MB", null, null);
            }
            
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return new ImageUploadResponse(false, "User not found", null, null);
            }
            
            User user = userOptional.get();
            
            // Delete old image if exists
            if (user.getProfileImagePath() != null && !user.getProfileImagePath().isEmpty()) {
                try {
                    Path oldImagePath = Paths.get(user.getProfileImagePath());
                    Files.deleteIfExists(oldImagePath);
                } catch (IOException e) {
                    // Log but don't fail the upload
                    System.err.println("Failed to delete old image: " + e.getMessage());
                }
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath);
            
            // Update user record
            String imageUrl = baseUrl + "/api/v1/images/" + filename;
            user.setProfileImageUrl(imageUrl);
            user.setProfileImagePath(filePath.toString());
            user.setImageUploadDate(LocalDateTime.now());
            
            userRepository.save(user);
            
            return new ImageUploadResponse(true, "Image uploaded successfully", imageUrl, filePath.toString());
            
        } catch (IOException e) {
            return new ImageUploadResponse(false, "Failed to upload image: " + e.getMessage(), null, null);
        } catch (Exception e) {
            return new ImageUploadResponse(false, "Unexpected error: " + e.getMessage(), null, null);
        }
    }
    
    /**
     * Delete profile image
     */
    public AuthResponse deleteProfileImage(Long userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return new AuthResponse(false, "User not found");
            }
            
            User user = userOptional.get();
            
            if (user.getProfileImagePath() == null || user.getProfileImagePath().isEmpty()) {
                return new AuthResponse(false, "No profile image to delete");
            }
            
            // Delete physical file
            try {
                Path imagePath = Paths.get(user.getProfileImagePath());
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Failed to delete image file: " + e.getMessage());
                // Continue with database update even if file deletion fails
            }
            
            // Update user record
            user.setProfileImageUrl(null);
            user.setProfileImagePath(null);
            user.setImageUploadDate(null);
            
            userRepository.save(user);
            
            return new AuthResponse(true, "Profile image deleted successfully");
            
        } catch (Exception e) {
            return new AuthResponse(false, "Failed to delete image: " + e.getMessage());
        }
    }
    
    /**
     * Get all users (for admin purposes)
     */
    public java.util.List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole(),
                    user.getProfileImageUrl(),
                    user.getImageUploadDate(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
                ))
                .toList();
    }
}
