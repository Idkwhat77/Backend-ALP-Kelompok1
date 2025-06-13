package com.ruangkerja.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request payload")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(description = "Username or email address", example = "johndoe", required = true)
    private String identifier;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "password123", required = true)
    private String password;
}
