package com.ruangkerja.rest.controller;

import com.ruangkerja.rest.dto.CompanyFormRequest;
import com.ruangkerja.rest.dto.EmployeeRequest;
import com.ruangkerja.rest.entity.Company;
import com.ruangkerja.rest.entity.Candidate;
import com.ruangkerja.rest.entity.User;
import com.ruangkerja.rest.repository.CompanyRepository;
import com.ruangkerja.rest.repository.CandidateRepository;
import com.ruangkerja.rest.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Add this import
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j // Add this annotation for logging
public class CompanyController {

    private static final String SUCCESS_KEY = "success";
    private static final String MESSAGE_KEY = "message";
    private static final String COMPANY_KEY = "company";
    private static final String COMPANIES_KEY = "companies";
    private static final String IMAGE_URL_KEY = "imageUrl";
    private static final String ERROR_TYPE_KEY = "errorType";

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

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
            company.setProvince(companyForm.getProvince());
            company.setCity(companyForm.getCity());
            company.setIndustry(companyForm.getIndustry());
            company.setCompanySize(companyForm.getCompanySize());
            company.setDescription(companyForm.getDescription());
            company.setWebsiteUrl(companyForm.getWebsiteUrl());
            company.setPhoneNumber(companyForm.getPhoneNumber());
            company.setCompanyType(companyForm.getCompanyType());

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
    // Enhanced get all companies with filtering
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCompanies(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "industry", required = false) String industry,
            @RequestParam(value = "sizeCategory", required = false) String sizeCategory,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "companyName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {
        try {
            Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(direction), sort));

            Page<Company> companies = companyRepository.findCompaniesWithFilters(
                search, province, city, industry, sizeCategory, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(COMPANIES_KEY, companies.getContent());
            response.put("totalElements", companies.getTotalElements());
            response.put("totalPages", companies.getTotalPages());
            response.put("currentPage", companies.getNumber());
            response.put("pageSize", companies.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to fetch companies: " + ex.getMessage());
        }
    }

    // Get featured companies
    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedCompanies() {
        try {
            List<Company> companies = companyRepository.findByIsFeatured(true);
            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(COMPANIES_KEY, companies);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to fetch featured companies: " + ex.getMessage());
        }
    }

    // Get unique provinces for filter dropdown
    @GetMapping("/provinces")
    public ResponseEntity<Map<String, Object>> getProvinces() {
        try {
            List<String> provinces = companyRepository.findAll()
                .stream()
                .map(Company::getProvince)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put("provinces", provinces);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to fetch provinces: " + ex.getMessage());
        }
    }

    // Get cities by province
    @GetMapping("/cities/{province}")
    public ResponseEntity<Map<String, Object>> getCitiesByProvince(@PathVariable String province) {
        try {
            List<String> cities = companyRepository.findByProvince(province)
                .stream()
                .map(Company::getCity)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put("cities", cities);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return error("ServerError", "Failed to fetch cities: " + ex.getMessage());
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
                // Use the same response format as getCompanyByUserId
                response.put(COMPANY_KEY, createCompanyResponse(company.get()));
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
                // Instead of returning the entity directly, map to a DTO or filter fields
                response.put(COMPANY_KEY, createCompanyResponse(company.get()));
                return ResponseEntity.ok(response);
            }
            return error("CompanyNotFound", "Company not found for this user");
        } catch (Exception ex) {
            return error("ServerError", "Failed to fetch company: " + ex.getMessage());
        }
    }

    // Add this helper method:
    private Map<String, Object> createCompanyResponse(Company company) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", company.getId());
        dto.put("companyName", company.getCompanyName());
        dto.put("email", company.getEmail());
        dto.put("industry", company.getIndustry());
        dto.put("companySize", company.getCompanySize());
        dto.put("sizeCategory", company.getSizeCategory());
        dto.put("profileImageUrl", company.getProfileImageUrl());
        dto.put("description", company.getDescription());
        dto.put("websiteUrl", company.getWebsiteUrl());
        dto.put("phoneNumber", company.getPhoneNumber());
        dto.put("hq", company.getHq());
        dto.put("province", company.getProvince());
        dto.put("city", company.getCity());
        dto.put("foundationDate", company.getFoundationDate());
        dto.put("isVerified", company.getIsVerified());
        dto.put("isFeatured", company.getIsFeatured());
        dto.put("createdAt", company.getCreatedAt());
        dto.put("updatedAt", company.getUpdatedAt());
        // ADD THIS LINE - Include userId for chat functionality
        dto.put("userId", company.getUser() != null ? company.getUser().getId() : null);
        // Optionally add user object as well
        dto.put("user", company.getUser() != null ? Map.of("id", company.getUser().getId()) : null);
        return dto;
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
            toUpdate.setProvince(companyForm.getProvince());
            toUpdate.setCity(companyForm.getCity());
            toUpdate.setIndustry(companyForm.getIndustry());
            toUpdate.setCompanySize(companyForm.getCompanySize());
            
            // Update optional fields only if provided
            if (companyForm.getDescription() != null) {
                toUpdate.setDescription(companyForm.getDescription());
            }
            if (companyForm.getWebsiteUrl() != null) {
                toUpdate.setWebsiteUrl(companyForm.getWebsiteUrl());
            }
            if (companyForm.getPhoneNumber() != null) {
                toUpdate.setPhoneNumber(companyForm.getPhoneNumber());
            }
            if (companyForm.getCompanyType() != null) {
                toUpdate.setCompanyType(companyForm.getCompanyType());
            }

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

    // --- Employee Management ---
    @PostMapping("/{companyId}/employees")
    @Operation(summary = "Add employee to company", description = "Add a candidate as an employee to the company")
    public ResponseEntity<Map<String, Object>> addEmployee(
            @PathVariable Long companyId,
            @Valid @RequestBody EmployeeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        if (userId == null) {
            return error("MissingUserId", "X-User-Id header is required");
        }

        try {
            // Verify company ownership
            Optional<Company> companyOptional = companyRepository.findById(companyId);
            if (companyOptional.isEmpty()) {
                return error("CompanyNotFound", "Company not found");
            }

            Company company = companyOptional.get();
            if (!company.getUser().getId().equals(userId)) {
                return error("Forbidden", "Forbidden: Not your company");
            }

            // Find candidate
            Optional<Candidate> candidateOptional = candidateRepository.findById(request.getCandidateId());
            if (candidateOptional.isEmpty()) {
                return error("CandidateNotFound", "Candidate not found");
            }

            Candidate candidate = candidateOptional.get();

            // Check if candidate is already an employee of another company
            if (candidate.getEmployerCompany() != null && candidate.getIsActiveEmployee()) {
                return error("AlreadyEmployed", "Candidate is already an active employee of another company");
            }

            // Set employee details
            candidate.setEmployerCompany(company);
            candidate.setPosition(request.getPosition());
            candidate.setDepartment(request.getDepartment());
            candidate.setHireDate(request.getHireDate() != null ? request.getHireDate() : LocalDate.now());
            candidate.setEmployeeId(request.getEmployeeId());
            candidate.setIsActiveEmployee(true);

            candidateRepository.save(candidate);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Employee added successfully");
            response.put("employee", createEmployeeResponse(candidate));

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return error("ServerError", "Failed to add employee: " + ex.getMessage());
        }
    }    @GetMapping("/{companyId}/employees")
    @Operation(summary = "Get company employees", description = "Get all employees of a company")
    public ResponseEntity<?> getCompanyEmployees(@PathVariable Long companyId, @RequestHeader("X-User-Id") Long userId) {
        try {
            // Replace companyService call with direct repository call
            Optional<Company> companyOptional = companyRepository.findById(companyId);
            if (companyOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Company not found"));
            }

            Company company = companyOptional.get();

            // Use repository method that doesn't trigger lazy loading issues
            List<Candidate> employees = candidateRepository.findByEmployerCompanyIdAndIsActiveEmployee(companyId, true);
            
            // Convert to DTOs to avoid lazy loading serialization
            List<Map<String, Object>> employeeDTOs = employees.stream().map(this::createEmployeeResponse).collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "employees", employeeDTOs
            ));
        } catch (Exception e) {
            log.error("Error getting company employees: ", e); // Use log instead of logger
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error loading employees"));
        }
    }

    @DeleteMapping("/{companyId}/employees/{candidateId}")
    @Operation(summary = "Remove employee", description = "Remove an employee from the company")
    public ResponseEntity<Map<String, Object>> removeEmployee(
            @PathVariable Long companyId,
            @PathVariable Long candidateId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        if (userId == null) {
            return error("MissingUserId", "X-User-Id header is required");
        }

        try {
            // Verify company ownership
            Optional<Company> companyOptional = companyRepository.findById(companyId);
            if (companyOptional.isEmpty()) {
                return error("CompanyNotFound", "Company not found");
            }

            Company company = companyOptional.get();
            if (!company.getUser().getId().equals(userId)) {
                return error("Forbidden", "Forbidden: Not your company");
            }

            // Find employee
            Optional<Candidate> candidateOptional = candidateRepository.findById(candidateId);
            if (candidateOptional.isEmpty()) {
                return error("CandidateNotFound", "Employee not found");
            }

            Candidate employee = candidateOptional.get();

            // Verify employee belongs to this company
            if (employee.getEmployerCompany() == null || 
                !employee.getEmployerCompany().getId().equals(companyId)) {
                return error("NotEmployee", "Candidate is not an employee of this company");
            }

            // Remove employment relationship
            employee.setEmployerCompany(null);
            employee.setPosition(null);
            employee.setDepartment(null);
            employee.setEmployeeId(null);
            employee.setIsActiveEmployee(false);

            candidateRepository.save(employee);

            Map<String, Object> response = new HashMap<>();
            response.put(SUCCESS_KEY, true);
            response.put(MESSAGE_KEY, "Employee removed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return error("ServerError", "Failed to remove employee: " + ex.getMessage());
        }
    }    // Helper method to create employee response
    private Map<String, Object> createEmployeeResponse(Candidate employee) {
        Map<String, Object> employeeResponse = new HashMap<>();
        employeeResponse.put("id", employee.getId());
        employeeResponse.put("fullName", employee.getFullName());
        employeeResponse.put("email", employee.getEmail());
        employeeResponse.put("position", employee.getPosition());
        employeeResponse.put("department", employee.getDepartment());
        employeeResponse.put("employeeId", employee.getEmployeeId());
        employeeResponse.put("hireDate", employee.getHireDate());
        employeeResponse.put("profileImageUrl", employee.getProfileImageUrl());
        employeeResponse.put("industry", employee.getIndustry());
        employeeResponse.put("city", employee.getCity());
        employeeResponse.put("province", employee.getProvince());
        employeeResponse.put("birthDate", employee.getBirthDate());
        employeeResponse.put("jobType", employee.getJobType());
        employeeResponse.put("employmentStatus", employee.getEmploymentStatus());
        return employeeResponse;
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