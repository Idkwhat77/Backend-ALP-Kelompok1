package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Email(message = "Email should be valid")
    private String email;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;
}
