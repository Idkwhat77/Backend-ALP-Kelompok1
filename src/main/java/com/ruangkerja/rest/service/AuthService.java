package com.ruangkerja.rest.service;

import com.ruangkerja.rest.dto.*;
import com.ruangkerja.rest.entity.User;
import com.ruangkerja.rest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    
    public AuthResponse register(RegisterRequest request) {
        try {
            // Check if passwords match
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return new AuthResponse(false, "Passwords do not match");
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return new AuthResponse(false, "Email is already registered");            }
            
            // Create new user
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword()); // In production, hash this password
            user.setPhone(request.getPhone());
            user.setRole("USER");
            
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
            
            return new AuthResponse(true, "Registration successful", userResponse);
            
        } catch (Exception e) {
            return new AuthResponse(false, "Registration failed: " + e.getMessage());
        }
    }
    
    public AuthResponse login(LoginRequest request) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            
            if (userOptional.isEmpty()) {
                return new AuthResponse(false, "User not found");
            }
            
            User user = userOptional.get();
            
            // Check password (In production, use proper password hashing)
            if (!user.getPassword().equals(request.getPassword())) {
                return new AuthResponse(false, "Invalid password");            }
            
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
            
            return new AuthResponse(true, "Login successful", userResponse);
            
        } catch (Exception e) {
            return new AuthResponse(false, "Login failed: " + e.getMessage());
        }
    }
}
