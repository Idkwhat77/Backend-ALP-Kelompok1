package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.entity.User;
import com.ruangkerja.rest.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus; // Added import
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "User API", description = "API endpoints for user profile management")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String USER_KEY = "user";
    private static final String IMAGE_URL_KEY = "imageUrl";
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/images/}")
    private String uploadDir;

    @PostMapping("/upload-image/{userId}")
    @Operation(summary = "Upload profile image", description = "Upload a profile image for the specified user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or user not found"),
            @ApiResponse(responseCode = "500", description = "File upload failed")
    })
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile file) {
        
        try {
            // Validate user exists
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse(USER_NOT_FOUND));
            }

            User user = userOptional.get();

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Please select a file to upload"));
            }

            // Check file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(createErrorResponse("File size exceeds 5MB limit"));
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !isValidImageType(contentType)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid file type. Only JPG, PNG, GIF, WebP are allowed"));
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid file name"));
            }
            String cleanedOriginalFilename = StringUtils.cleanPath(originalFilename);
            String fileExtension = getFileExtension(cleanedOriginalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "_" + userId + "." + fileExtension;

            // Delete old profile image if exists
            if (user.getProfileImagePath() != null) {
                deleteOldProfileImage(user.getProfileImagePath());
            }

            // Save file
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Update user profile image info
            String imageUrl = "/api/v1/images/" + uniqueFilename;
            user.setProfileImageUrl(imageUrl);
            user.setProfileImagePath(targetLocation.toString());
            user.setImageUploadDate(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Profile image uploaded successfully");
            response.put(IMAGE_URL_KEY, imageUrl);
            response.put(USER_KEY, createUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (IOException ex) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to upload file: " + ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Image upload failed"));
        }
    }

    @DeleteMapping("/delete-image/{userId}")
    @Operation(summary = "Delete profile image", description = "Delete the profile image for the specified user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "400", description = "User not found"),
            @ApiResponse(responseCode = "404", description = "No image to delete")
    })
    public ResponseEntity<Map<String, Object>> deleteProfileImage(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse(USER_NOT_FOUND));
            }

            User user = userOptional.get();

            if (user.getProfileImagePath() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("No profile image to delete"));
            }

            // Delete physical file
            deleteOldProfileImage(user.getProfileImagePath());

            // Clear user profile image info
            user.setProfileImageUrl(null);
            user.setProfileImagePath(null);
            user.setImageUploadDate(null);
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Profile image deleted successfully");
            response.put(USER_KEY, createUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(createErrorResponse("Failed to delete image"));
        }
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "Get user profile", description = "Get user profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(USER_NOT_FOUND)); // Use HttpStatus
            }

            User user = userOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(USER_KEY, createUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            logger.error("Failed to retrieve profile for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to retrieve profile")); // Use HttpStatus
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/svg+xml");
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg"; // default extension
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void deleteOldProfileImage(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            logger.error("Failed to delete old profile image: {}", imagePath, ex);
        }
    }

    private Map<String, Object> createErrorResponse(String message) { // Return Map<String, Object>
        Map<String, Object> response = new HashMap<>(); // Use Object for value
        response.put("error", message);
        return response;
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("isActive", user.getIsActive());
        userResponse.put("profileImageUrl", user.getProfileImageUrl());
        userResponse.put("imageUploadDate", user.getImageUploadDate());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        return userResponse;
    }

    private Map<String, Object> createSuccessResponse(String message) { // Return Map<String, Object>
        Map<String, Object> response = new HashMap<>(); // Use Object for value
        response.put(MESSAGE_KEY, message); // Use MESSAGE_KEY constant
        return response;
    }

    @GetMapping("/phone/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPhone(@PathVariable Long userId) { // Changed userId to Long, return type to Map<String, Object>
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(USER_NOT_FOUND));
        }
        User user = userOptional.get();
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("Phone number not found for this user"));
        }
        Map<String, Object> response = new HashMap<>(); // Use Object for value
        response.put("phone", user.getPhone());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/phone/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserPhone(@PathVariable Long userId, @RequestBody Map<String, String> payload) { // Changed userId to Long, return type to Map<String, Object>
        String phone = payload.get("phone");
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Phone number cannot be empty"));
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(USER_NOT_FOUND));
        }

        User user = userOptional.get();
        user.setPhone(phone);
        userRepository.save(user);

        return ResponseEntity.ok(createSuccessResponse("Phone number updated successfully"));
    }
}
