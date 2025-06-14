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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;

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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(USER_NOT_FOUND));
            }

            User user = userOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(USER_KEY, createUserResponse(user));

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            logger.error("Failed to retrieve profile for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("Failed to retrieve profile"));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("isActive", user.getIsActive());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("updatedAt", user.getUpdatedAt());
        return userResponse;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(MESSAGE_KEY, message);
        return response;
    }

    @GetMapping("/phone/{userId}")
    public ResponseEntity<Map<String, Object>> getUserPhone(@PathVariable Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(USER_NOT_FOUND));
        }
        User user = userOptional.get();
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("Phone number not found for this user"));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("phone", user.getPhone());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/phone/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserPhone(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
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