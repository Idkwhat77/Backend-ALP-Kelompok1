package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.CompanyFormRequest;
import com.ruangkerja.rest.entity.Company;
import com.ruangkerja.rest.entity.User;
import com.ruangkerja.rest.repository.CompanyRepository;
import com.ruangkerja.rest.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompanyController {

    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String COMPANY_KEY = "company";
    private static final String COMPANIES_KEY = "companies";
    private static final String IMAGE_URL_KEY = "imageUrl";
    private static final String ERROR_TYPE_KEY = "errorType";

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/images/}")
    private String uploadDir;

    // --- Global Exception Handler for Validation ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(ERROR_TYPE_KEY, "ValidationError");
        response.put("errors", errors);
        response.put(MESSAGE_KEY, "Validation failed");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(ERROR_TYPE_KEY, "ConstraintViolation");
        response.put("errors", errors);
        response.put(MESSAGE_KEY, "Constraint violation");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(ERROR_TYPE_KEY, "DataIntegrityViolation");
        response.put(MESSAGE_KEY, "Database constraint error: " + ex.getRootCause().getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOtherExceptions(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(ERROR_TYPE_KEY, "ServerError");
        response.put(MESSAGE_KEY, ex.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    // --- Create company with userId (header) ---
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCompany(
            @Valid @RequestBody CompanyFormRequest companyForm,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return error("MissingUserId", "X-User-Id header is required");
        }
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return error("UserNotFound", "User not found");
        }
        if (companyRepository.existsByUserId(userId)) {
            return error("CompanyExists", "Company already exists for this user");
        }
        try {
            Company company = new Company();
            company.setUser(userOptional.get());
            company.setCompanyName(companyForm.getCompanyName());
            company.setEmail(companyForm.getEmail());
            company.setFoundationDate(companyForm.getFoundationDate());
            company.setHq(companyForm.getHq());
            company.setIndustry(companyForm.getIndustry());
            company.setCompanySize(companyForm.getCompanySize());

            Company saved = companyRepository.save(company);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(COMPANY_KEY, saved);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to create company: " + ex.getMessage());
        }
    }

    // --- Get all companies ---
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCompanies() {
        try {
            List<Company> companies = companyRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(COMPANIES_KEY, companies);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to fetch companies: " + ex.getMessage());
        }
    }

    // --- Get company by ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCompanyById(@PathVariable Long id) {
        try {
            Optional<Company> company = companyRepository.findById(id);
            if (company.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS_KEY, true);
                response.put(COMPANY_KEY, company.get());
                return ResponseEntity.ok(response);
            }
            return error("CompanyNotFound", "Company not found");
        } catch (Exception ex) {
            return error("ServerError", "Failed to fetch company: " + ex.getMessage());
        }
    }

    // --- Get company by user ID ---
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getCompanyByUserId(@PathVariable Long userId) {
        try {
            Optional<Company> company = companyRepository.findByUserId(userId);
            if (company.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put(SUCCESS_KEY, true);
                response.put(COMPANY_KEY, company.get());
                return ResponseEntity.ok(response);
            }
            return error("CompanyNotFound", "Company not found for this user");
        } catch (Exception ex) {
            return error("ServerError", "Failed to fetch company: " + ex.getMessage());
        }
    }

    // --- Update company (requires userId header) ---
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyFormRequest companyForm,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return error("MissingUserId", "X-User-Id header is required");
        }
        Optional<Company> existing = companyRepository.findById(id);
        if (existing.isEmpty()) {
            return error("CompanyNotFound", "Company not found");
        }
        Company toUpdate = existing.get();
        if (!toUpdate.getUser().getId().equals(userId)) {
            return error("Forbidden", "Forbidden: Not your company");
        }
        try {
            toUpdate.setCompanyName(companyForm.getCompanyName());
            toUpdate.setEmail(companyForm.getEmail());
            toUpdate.setFoundationDate(companyForm.getFoundationDate());
            toUpdate.setHq(companyForm.getHq());
            toUpdate.setIndustry(companyForm.getIndustry());
            toUpdate.setCompanySize(companyForm.getCompanySize());

            Company updated = companyRepository.save(toUpdate);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(COMPANY_KEY, updated);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to update company: " + ex.getMessage());
        }
    }

    // --- Delete company ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCompany(@PathVariable Long id, @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return error("MissingUserId", "X-User-Id header is required");
        }
        Optional<Company> companyOpt = companyRepository.findById(id);
        if (companyOpt.isEmpty()) {
            return error("CompanyNotFound", "Company not found");
        }
        Company company = companyOpt.get();
        if (!company.getUser().getId().equals(userId)) {
            return error("Forbidden", "Forbidden: Not your company");
        }
        try {
            companyRepository.deleteById(id);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Company deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to delete company: " + ex.getMessage());
        }
    }

    // --- Profile Image Upload ---
    @PostMapping("/upload-image/{companyId}")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @PathVariable Long companyId,
            @RequestParam(value = "image", required = false) MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return error("MissingUserId", "X-User-Id header is required");
        }
        Optional<Company> companyOptional = companyRepository.findById(companyId);
        if (companyOptional.isEmpty()) {
            return error("CompanyNotFound", "Company not found");
        }
        Company company = companyOptional.get();
        if (!company.getUser().getId().equals(userId)) {
            return error("Forbidden", "Forbidden: Not your company");
        }
        if (file == null) {
            return error("MissingFile", "No file uploaded");
        }
        if (file.isEmpty()) {
            return error("EmptyFile", "Please select a file to upload");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return error("FileTooLarge", "File size exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            return error("InvalidFileType", "Invalid file type. Only JPG, PNG, GIF, WebP are allowed");
        }
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return error("InvalidFileName", "Invalid file name");
            }
            String cleanedOriginalFilename = StringUtils.cleanPath(originalFilename);
            String fileExtension = getFileExtension(cleanedOriginalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "_" + companyId + "." + fileExtension;
            if (company.getProfileImagePath() != null) {
                deleteOldProfileImage(company.getProfileImagePath());
            }
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            String imageUrl = "/api/v1/images/" + uniqueFilename;
            company.setProfileImageUrl(imageUrl);
            company.setProfileImagePath(targetLocation.toString());
            company.setImageUploadDate(LocalDateTime.now());
            companyRepository.save(company);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Profile image uploaded successfully");
            response.put(IMAGE_URL_KEY, imageUrl);
            response.put(COMPANY_KEY, company);
            return ResponseEntity.ok(response);
        } catch (IOException ex) {
            return error("FileUploadError", "Failed to upload file: " + ex.getMessage());
        } catch (Exception ex) {
            return error("ServerError", "Image upload failed: " + ex.getMessage());
        }
    }

    // --- Delete Profile Image ---
    @DeleteMapping("/delete-image/{companyId}")
    public ResponseEntity<Map<String, Object>> deleteProfileImage(@PathVariable Long companyId, @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return error("MissingUserId", "X-User-Id header is required");
        }
        Optional<Company> companyOptional = companyRepository.findById(companyId);
        if (companyOptional.isEmpty()) {
            return error("CompanyNotFound", "Company not found");
        }
        Company company = companyOptional.get();
        if (!company.getUser().getId().equals(userId)) {
            return error("Forbidden", "Forbidden: Not your company");
        }
        if (company.getProfileImagePath() == null) {
            return error("NoImage", "No profile image to delete");
        }
        try {
            deleteOldProfileImage(company.getProfileImagePath());
            company.setProfileImageUrl(null);
            company.setProfileImagePath(null);
            company.setImageUploadDate(null);
            companyRepository.save(company);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Profile image deleted successfully");
            response.put(COMPANY_KEY, company);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to delete image: " + ex.getMessage());
        }
    }

    // --- Update Description ---
    @PutMapping("/{companyId}/description")
    public ResponseEntity<Map<String, Object>> updateDescription(
            @PathVariable Long companyId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return error("MissingUserId", "X-User-Id header is required");
        }
        Optional<Company> companyOptional = companyRepository.findById(companyId);
        if (companyOptional.isEmpty()) {
            return error("CompanyNotFound", "Company not found");
        }
        Company company = companyOptional.get();
        if (!company.getUser().getId().equals(userId)) {
            return error("Forbidden", "Forbidden: Not your company");
        }
        String description = request.get("description");
        if (description == null || description.trim().isEmpty()) {
            return error("EmptyDescription", "Description cannot be empty");
        }
        try {
            company.setDescription(description.trim());
            companyRepository.save(company);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Description updated successfully");
            response.put(COMPANY_KEY, company);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to update description: " + ex.getMessage());
        }
    }

    // --- Helper Methods ---
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
            // log error if needed
        }
    }

    private ResponseEntity<Map<String, Object>> error(String errorType, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put(SUCCESS_KEY, false);
        response.put(ERROR_TYPE_KEY, errorType);
        response.put(MESSAGE_KEY, message);
        return ResponseEntity.badRequest().body(response);
    }
}