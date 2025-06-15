package com.ruangkerja.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HobbyRequest {
    
    @NotBlank(message = "Hobby name is required")
    @Size(min = 2, max = 100, message = "Hobby name must be between 2 and 100 characters")
    private String name;
}