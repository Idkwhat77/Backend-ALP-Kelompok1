package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.PortfolioRequest;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.entity.Portfolio;
import com.ruangkerja.rest.repository.CandidateRepository;
import com.ruangkerja.rest.repository.PortfolioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Portfolio API", description = "API endpoints for managing candidate portfolio items")
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);
    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String PORTFOLIO_KEY = "portfolio";
    private static final String IMAGE_URL_KEY = "imageUrl";
    private static final String ERROR_TYPE_KEY = "errorType";
    private static final String ERROR_DETAILS_KEY = "errorDetails";
    
    private final PortfolioRepository portfolioRepository;
    private final CandidateRepository candidateRepository;

    // Use the same upload directory as CandidateController
    @Value("${app.upload.dir:uploads/images/}")
    private String uploadDir;

    // Global exception handlers for this controller
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        logger.error("File upload size exceeded: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(createDetailedErrorResponse(
            "FILE_TOO_LARGE", 
            "File size exceeds maximum allowed size (5MB)",
            "Maximum file size is 5MB. Please choose a smaller file."
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        logger.error("File access denied: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
            "FILE_ACCESS_DENIED",
            "Permission denied accessing file system",
            "The server doesn't have permission to write files. Contact administrator."
        ));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        logger.error("IO error during file operation: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
            "FILE_IO_ERROR",
            "File input/output error: " + ex.getMessage(),
            "Failed to read or write file. Please try again."
        ));
    }

    @Operation(summary = "Get candidate's portfolio items", description = "Retrieves all portfolio items for a specific candidate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio items retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Candidate not found"),
        @ApiResponse(responseCode = "400", description = "Invalid candidate ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{candidateId}/portfolio")
    public ResponseEntity<Map<String, Object>> getCandidatePortfolio(
            @Parameter(description = "Candidate ID") @PathVariable Long candidateId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("=== GET PORTFOLIO START - Candidate ID: {} ===", candidateId);
            
            // Validate candidate ID
            if (candidateId == null || candidateId <= 0) {
                logger.warn("Invalid candidate ID provided: {}", candidateId);
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "INVALID_CANDIDATE_ID",
                    "Invalid candidate ID: " + candidateId,
                    "Candidate ID must be a positive number"
                ));
            }
            
            // Check if candidate exists
            if (!candidateRepository.existsById(candidateId)) {
                logger.warn("Candidate not found for ID: {}", candidateId);
                return ResponseEntity.status(404).body(createDetailedErrorResponse(
                    "CANDIDATE_NOT_FOUND",
                    "Candidate not found with ID: " + candidateId,
                    "The specified candidate does not exist"
                ));
            }
            
            // Get portfolio items
            List<Portfolio> portfolioItems = portfolioRepository.findByCandidateIdOrderByCreatedAtDesc(candidateId);
            logger.info("Found {} portfolio items for candidate ID: {}", portfolioItems.size(), candidateId);
            
            // Convert to DTOs
            List<Map<String, Object>> portfolioDTOs = portfolioItems.stream()
                .map(this::convertToDTO)
                .toList();
            
            response.put(SUCCESS_KEY, true);
            response.put(PORTFOLIO_KEY, portfolioDTOs);
            response.put(MESSAGE_KEY, "Portfolio items retrieved successfully");
            
            logger.info("=== GET PORTFOLIO SUCCESS ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("=== GET PORTFOLIO ERROR ===", e);
            return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                "DATABASE_ERROR",
                "Database error: " + e.getMessage(),
                "Failed to retrieve portfolio items from database"
            ));
        }
    }

    @Operation(summary = "Add portfolio item", description = "Adds a new portfolio item for a candidate")
    @PostMapping("/portfolio")
    @Transactional
    public ResponseEntity<Map<String, Object>> addCandidatePortfolio(
            @Valid @RequestBody PortfolioRequest request) {
        
        try {
            logger.info("=== ADD PORTFOLIO START - Candidate ID: {} ===", request.getCandidateId());
            logger.info("Portfolio data: title={}, url={}, type={}", request.getTitle(), request.getUrl(), request.getType());
            
            // Validate candidate exists
            Optional<Candidate> candidateOpt = candidateRepository.findById(request.getCandidateId());
            if (candidateOpt.isEmpty()) {
                logger.warn("Candidate not found for ID: {}", request.getCandidateId());
                return ResponseEntity.status(404).body(createDetailedErrorResponse(
                    "CANDIDATE_NOT_FOUND",
                    "Candidate not found with ID: " + request.getCandidateId(),
                    "The specified candidate does not exist"
                ));
            }
            
            Candidate candidate = candidateOpt.get();
            logger.info("Found candidate: {}", candidate.getFullName());
            
            // Create new portfolio item
            Portfolio portfolio = new Portfolio();
            portfolio.setTitle(request.getTitle().trim());
            portfolio.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            portfolio.setUrl(request.getUrl().trim());
            portfolio.setType(request.getType() != null ? request.getType().trim() : null);
            portfolio.setImageUrl(request.getImageUrl());
            portfolio.setCandidate(candidate);
            
            Portfolio savedPortfolio = portfolioRepository.save(portfolio);
            logger.info("Portfolio saved with ID: {}", savedPortfolio.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Portfolio item added successfully");
            response.put(PORTFOLIO_KEY, convertToDTO(savedPortfolio));
            
            logger.info("=== ADD PORTFOLIO SUCCESS ===");
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            logger.error("=== ADD PORTFOLIO ERROR ===", e);
            return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                "SAVE_ERROR",
                "Failed to save portfolio: " + e.getMessage(),
                "Could not save portfolio item to database"
            ));
        }
    }

    @Operation(summary = "Upload portfolio image", description = "Uploads an image for a portfolio item")
    @PostMapping("/portfolio/{portfolioId}/upload-image")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadPortfolioImage(
            @Parameter(description = "Portfolio ID") @PathVariable Long portfolioId,
            @RequestParam("image") MultipartFile file) {
        
        try {
            logger.info("=== PORTFOLIO IMAGE UPLOAD START ===");
            logger.info("Portfolio ID: {}", portfolioId);
            logger.info("File original name: {}", file.getOriginalFilename());
            logger.info("File size: {} bytes", file.getSize());
            logger.info("File content type: {}", file.getContentType());
            logger.info("File is empty: {}", file.isEmpty());
            
            // Validate portfolio ID
            if (portfolioId == null || portfolioId <= 0) {
                logger.error("Invalid portfolio ID: {}", portfolioId);
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "INVALID_PORTFOLIO_ID",
                    "Invalid portfolio ID: " + portfolioId,
                    "Portfolio ID must be a positive number"
                ));
            }
            
            // Find portfolio by ID
            Optional<Portfolio> portfolioOpt = portfolioRepository.findById(portfolioId);
            if (portfolioOpt.isEmpty()) {
                logger.error("Portfolio not found with ID: {}", portfolioId);
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "PORTFOLIO_NOT_FOUND",
                    "Portfolio item not found with ID: " + portfolioId,
                    "The specified portfolio item does not exist"
                ));
            }
            
            Portfolio portfolio = portfolioOpt.get();
            logger.info("Found portfolio: {} (Candidate: {})", portfolio.getTitle(), portfolio.getCandidate().getFullName());
            
            // Validate file
            if (file == null) {
                logger.error("File parameter is null");
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "NULL_FILE",
                    "File parameter is null",
                    "No file was provided in the request"
                ));
            }
            
            if (file.isEmpty()) {
                logger.error("File is empty");
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "EMPTY_FILE",
                    "Uploaded file is empty",
                    "Please select a valid image file"
                ));
            }

            // Check file size (5MB limit)
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxSize) {
                logger.error("File size too large: {} bytes (max: {} bytes)", file.getSize(), maxSize);
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "FILE_TOO_LARGE",
                    "File size " + file.getSize() + " bytes exceeds maximum " + maxSize + " bytes",
                    "File size must be less than 5MB"
                ));
            }

            // Check file type
            String contentType = file.getContentType();
            logger.info("Validating content type: {}", contentType);
            if (contentType == null) {
                logger.error("Content type is null");
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "NULL_CONTENT_TYPE",
                    "File content type is null",
                    "Unable to determine file type"
                ));
            }
            
            if (!isValidImageType(contentType)) {
                logger.error("Invalid content type: {}", contentType);
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "INVALID_FILE_TYPE",
                    "Invalid file type: " + contentType,
                    "Only JPG, PNG, GIF, WebP are allowed"
                ));
            }

            // Create upload directory - DETAILED LOGGING
            logger.info("Upload directory from config: {}", uploadDir);
            Path uploadPath = Paths.get(uploadDir);
            logger.info("Upload path resolved to: {}", uploadPath.toAbsolutePath());
            logger.info("Upload directory exists: {}", Files.exists(uploadPath));
            logger.info("Upload directory is directory: {}", Files.isDirectory(uploadPath));
            logger.info("Upload directory is writable: {}", Files.isWritable(uploadPath));
            
            try {
                if (!Files.exists(uploadPath)) {
                    logger.info("Creating upload directory...");
                    Files.createDirectories(uploadPath);
                    logger.info("Upload directory created successfully");
                    
                    // Verify creation
                    if (!Files.exists(uploadPath)) {
                        throw new IOException("Directory was not created");
                    }
                    logger.info("Directory creation verified");
                }
                
                // Test write permissions
                if (!Files.isWritable(uploadPath)) {
                    throw new AccessDeniedException("Upload directory is not writable: " + uploadPath.toAbsolutePath());
                }
                logger.info("Upload directory is writable");
                
            } catch (IOException e) {
                logger.error("Failed to create or access upload directory: {}", uploadPath.toAbsolutePath(), e);
                return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                    "DIRECTORY_CREATION_FAILED",
                    "Failed to create upload directory: " + e.getMessage(),
                    "Server cannot create or access the upload directory"
                ));
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                logger.warn("Original filename is null, using default");
                originalFilename = "portfolio_image";
            }
            logger.info("Original filename: {}", originalFilename);
            
            String cleanedOriginalFilename = StringUtils.cleanPath(originalFilename);
            logger.info("Cleaned filename: {}", cleanedOriginalFilename);
            
            String fileExtension = getFileExtension(cleanedOriginalFilename);
            logger.info("File extension: {}", fileExtension);
            
            String uniqueFilename = "portfolio_" + UUID.randomUUID().toString() + "_" + portfolioId + "." + fileExtension;
            logger.info("Generated unique filename: {}", uniqueFilename);

            // Delete old portfolio image if exists
            if (portfolio.getImageUrl() != null && !portfolio.getImageUrl().trim().isEmpty()) {
                logger.info("Deleting old portfolio image: {}", portfolio.getImageUrl());
                try {
                    deleteOldPortfolioImage(portfolio.getImageUrl());
                    logger.info("Old image deletion completed");
                } catch (Exception e) {
                    logger.warn("Failed to delete old image, continuing with upload: {}", e.getMessage());
                }
            }

            // Save file
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            logger.info("Target file location: {}", targetLocation.toAbsolutePath());
            
            try {
                // Ensure parent directory exists
                Files.createDirectories(targetLocation.getParent());
                
                // Copy file
                try (InputStream inputStream = file.getInputStream()) {
                    long bytesCopied = Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("File copied successfully. Bytes copied: {}", bytesCopied);
                }
                
                // Verify file was created and has correct size
                if (!Files.exists(targetLocation)) {
                    throw new IOException("File was not created at target location");
                }
                
                long savedFileSize = Files.size(targetLocation);
                logger.info("Saved file size: {} bytes (original: {} bytes)", savedFileSize, file.getSize());
                
                if (savedFileSize == 0) {
                    throw new IOException("Saved file is empty");
                }
                
                if (Math.abs(savedFileSize - file.getSize()) > 1000) { // Allow small difference for metadata
                    logger.warn("Significant file size difference detected");
                }
                
            } catch (IOException e) {
                logger.error("Failed to save file to: {}", targetLocation.toAbsolutePath(), e);
                return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                    "FILE_SAVE_FAILED",
                    "Failed to save file: " + e.getMessage(),
                    "Could not save the uploaded file to server storage"
                ));
            }

            // Update portfolio with image URL
            String imageUrl = "/api/v1/images/" + uniqueFilename;
            logger.info("Setting portfolio image URL: {}", imageUrl);
            
            try {
                portfolio.setImageUrl(imageUrl);
                Portfolio savedPortfolio = portfolioRepository.save(portfolio);
                logger.info("Portfolio updated in database successfully");

                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS_KEY, true);
                response.put(MESSAGE_KEY, "Portfolio image uploaded successfully");
                response.put(IMAGE_URL_KEY, imageUrl);
                response.put(PORTFOLIO_KEY, convertToDTO(savedPortfolio));

                logger.info("=== PORTFOLIO IMAGE UPLOAD SUCCESS ===");
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                logger.error("Failed to update portfolio in database", e);
                
                // Clean up uploaded file since DB update failed
                try {
                    Files.deleteIfExists(targetLocation);
                    logger.info("Cleaned up uploaded file due to database error");
                } catch (IOException cleanupEx) {
                    logger.warn("Failed to clean up uploaded file: {}", cleanupEx.getMessage());
                }
                
                return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                    "DATABASE_UPDATE_FAILED",
                    "File uploaded but database update failed: " + e.getMessage(),
                    "Image was uploaded but could not be linked to portfolio item"
                ));
            }

        } catch (Exception ex) {
            logger.error("=== PORTFOLIO IMAGE UPLOAD UNEXPECTED ERROR ===", ex);
            return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                "UNEXPECTED_ERROR",
                "Unexpected error during image upload: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(),
                "An unexpected error occurred. Please try again or contact support."
            ));
        }
    }

    @DeleteMapping("/portfolio/{portfolioId}/delete-image")
    @Transactional
    public ResponseEntity<Map<String, Object>> deletePortfolioImage(
            @Parameter(description = "Portfolio ID") @PathVariable Long portfolioId) {
        
        try {
            logger.info("=== DELETE PORTFOLIO IMAGE START - Portfolio ID: {} ===", portfolioId);
            
            // Validate portfolio ID
            if (portfolioId == null || portfolioId <= 0) {
                logger.error("Invalid portfolio ID: {}", portfolioId);
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "INVALID_PORTFOLIO_ID",
                    "Invalid portfolio ID: " + portfolioId,
                    "Portfolio ID must be a positive number"
                ));
            }
            
            Optional<Portfolio> portfolioOpt = portfolioRepository.findById(portfolioId);
            if (portfolioOpt.isEmpty()) {
                logger.error("Portfolio not found with ID: {}", portfolioId);
                return ResponseEntity.badRequest().body(createDetailedErrorResponse(
                    "PORTFOLIO_NOT_FOUND",
                    "Portfolio item not found with ID: " + portfolioId,
                    "The specified portfolio item does not exist"
                ));
            }
            
            Portfolio portfolio = portfolioOpt.get();
            logger.info("Found portfolio: {} (Current image: {})", portfolio.getTitle(), portfolio.getImageUrl());
            
            String currentImageUrl = portfolio.getImageUrl();
            
            if (currentImageUrl != null && !currentImageUrl.trim().isEmpty()) {
                logger.info("Deleting image file: {}", currentImageUrl);
                try {
                    deleteOldPortfolioImage(currentImageUrl);
                    logger.info("Image file deleted successfully");
                } catch (Exception e) {
                    logger.warn("Failed to delete image file, but continuing with database update: {}", e.getMessage());
                }
                
                portfolio.setImageUrl(null);
                portfolioRepository.save(portfolio);
                logger.info("Portfolio image URL cleared in database");
            } else {
                logger.info("No image URL found, nothing to delete");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Portfolio image deleted successfully");
            response.put(PORTFOLIO_KEY, convertToDTO(portfolio));
            
            logger.info("=== DELETE PORTFOLIO IMAGE SUCCESS ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("=== DELETE PORTFOLIO IMAGE ERROR ===", e);
            return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                "DELETE_ERROR",
                "Failed to delete portfolio image: " + e.getMessage(),
                "Could not delete the portfolio image"
            ));
        }
    }

    @PutMapping("/portfolio/{portfolioId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateCandidatePortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioRequest request) {
        
        try {
            logger.info("=== UPDATE PORTFOLIO START - Portfolio ID: {} ===", portfolioId);
            
            Optional<Portfolio> portfolioOpt = portfolioRepository.findById(portfolioId);
            if (portfolioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(createDetailedErrorResponse(
                    "PORTFOLIO_NOT_FOUND",
                    "Portfolio item not found with ID: " + portfolioId,
                    "The specified portfolio item does not exist"
                ));
            }
            
            Portfolio portfolio = portfolioOpt.get();
            
            // Update portfolio fields
            portfolio.setTitle(request.getTitle().trim());
            portfolio.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
            portfolio.setUrl(request.getUrl().trim());
            portfolio.setType(request.getType() != null ? request.getType().trim() : null);
            
            // Only update imageUrl if provided in request
            if (request.getImageUrl() != null) {
                portfolio.setImageUrl(request.getImageUrl());
            }
            
            Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Portfolio item updated successfully");
            response.put(PORTFOLIO_KEY, convertToDTO(updatedPortfolio));
            
            logger.info("=== UPDATE PORTFOLIO SUCCESS ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("=== UPDATE PORTFOLIO ERROR ===", e);
            return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                "UPDATE_ERROR",
                "Failed to update portfolio: " + e.getMessage(),
                "Could not update the portfolio item"
            ));
        }
    }

    @DeleteMapping("/portfolio/{portfolioId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteCandidatePortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable Long portfolioId) {
        
        try {
            logger.info("=== DELETE PORTFOLIO START - Portfolio ID: {} ===", portfolioId);
            
            Optional<Portfolio> portfolioOpt = portfolioRepository.findById(portfolioId);
            if (portfolioOpt.isEmpty()) {
                return ResponseEntity.status(404).body(createDetailedErrorResponse(
                    "PORTFOLIO_NOT_FOUND",
                    "Portfolio item not found with ID: " + portfolioId,
                    "The specified portfolio item does not exist"
                ));
            }
            
            Portfolio portfolio = portfolioOpt.get();
            
            // Delete associated image file if exists
            if (portfolio.getImageUrl() != null) {
                logger.info("Deleting associated image: {}", portfolio.getImageUrl());
                try {
                    deleteOldPortfolioImage(portfolio.getImageUrl());
                } catch (Exception e) {
                    logger.warn("Failed to delete image file: {}", e.getMessage());
                }
            }
            
            portfolioRepository.delete(portfolio);
            
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Portfolio item deleted successfully");
            
            logger.info("=== DELETE PORTFOLIO SUCCESS ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("=== DELETE PORTFOLIO ERROR ===", e);
            return ResponseEntity.internalServerError().body(createDetailedErrorResponse(
                "DELETE_ERROR",
                "Failed to delete portfolio: " + e.getMessage(),
                "Could not delete the portfolio item"
            ));
        }
    }

    // Helper method to convert entity to DTO and avoid circular reference
    private Map<String, Object> convertToDTO(Portfolio portfolio) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", portfolio.getId());
        dto.put("title", portfolio.getTitle());
        dto.put("description", portfolio.getDescription());
        dto.put("url", portfolio.getUrl());
        dto.put("type", portfolio.getType());
        dto.put("imageUrl", portfolio.getImageUrl());
        dto.put("createdAt", portfolio.getCreatedAt());
        dto.put("updatedAt", portfolio.getUpdatedAt());
        dto.put("candidateId", portfolio.getCandidate().getId());
        return dto;
    }

    // Helper method to delete old portfolio images
    private void deleteOldPortfolioImage(String imageUrl) {
        try {
            if (imageUrl != null && imageUrl.startsWith("/api/v1/images/")) {
                String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                Path imagePath = Paths.get(uploadDir, filename);
                
                logger.info("Attempting to delete file: {}", imagePath.toAbsolutePath());
                
                if (Files.exists(imagePath)) {
                    Files.delete(imagePath);
                    logger.info("Successfully deleted old portfolio image: {}", filename);
                } else {
                    logger.warn("Portfolio image file not found for deletion: {}", imagePath.toAbsolutePath());
                }
            } else {
                logger.warn("Invalid image URL format for deletion: {}", imageUrl);
            }
        } catch (Exception e) {
            logger.error("Failed to delete old portfolio image: {}", imageUrl, e);
            throw new RuntimeException("Failed to delete old image: " + e.getMessage(), e);
        }
    }

    // Helper methods
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

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(MESSAGE_KEY, message);
        return response;
    }

    private Map<String, Object> createDetailedErrorResponse(String errorType, String message, String userFriendlyMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(ERROR_TYPE_KEY, errorType);
        response.put(MESSAGE_KEY, userFriendlyMessage);
        response.put(ERROR_DETAILS_KEY, message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}