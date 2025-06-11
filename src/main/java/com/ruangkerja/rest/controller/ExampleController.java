package com.ruangkerja.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Example API", description = "Sample API endpoints for demonstration")
public class ExampleController {

    @Operation(summary = "Get a greeting message", description = "Returns a simple greeting message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved greeting"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from RuangKerja API!");
    }

    @Operation(summary = "Get personalized greeting", description = "Returns a personalized greeting message")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved personalized greeting"),
        @ApiResponse(responseCode = "400", description = "Invalid name provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/hello/{name}")
    public ResponseEntity<String> sayHelloToName(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name cannot be empty");
        }
        return ResponseEntity.ok("Hello " + name + " from RuangKerja API!");
    }

    @Operation(summary = "Health check", description = "Check if the API is running")
    @ApiResponse(responseCode = "200", description = "API is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API is running successfully!");
    }
}
