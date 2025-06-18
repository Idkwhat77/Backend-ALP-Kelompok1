package com.ruangkerja.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    @NotBlank(message = "Position is required")
    private String position;

    private String department;  // Remove @NotBlank - make it optional

    private String employeeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    // ...getters and setters...
}