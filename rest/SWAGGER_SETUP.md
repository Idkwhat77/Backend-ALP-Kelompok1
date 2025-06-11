# RuangKerja API - Swagger/OpenAPI Documentation

## Overview
This Spring Boot application has been configured with Swagger/OpenAPI documentation using `springdoc-openapi-starter-webmvc-ui` version 2.8.5, which is compatible with Spring Boot 3.x.

## Installation Summary

### 1. Dependencies Added
Added to `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

### 2. Configuration Added
Added to `application.properties`:
```properties
# OpenAPI/Swagger Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

### 3. OpenAPI Configuration Class
Created `OpenApiConfig.java` to customize the API documentation metadata.

### 4. Example Controller
Created `ExampleController.java` with Swagger annotations to demonstrate the documentation features.

## Accessing the Documentation

### Swagger UI (Interactive Documentation)
- **URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Interactive interface to test API endpoints
- Visual representation of your API with try-it-out functionality

### OpenAPI JSON Specification
- **URL**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- Raw OpenAPI 3.0 specification in JSON format
- Can be imported into other tools like Postman, Insomnia, etc.

### OpenAPI YAML Specification
- **URL**: [http://localhost:8080/api-docs.yaml](http://localhost:8080/api-docs.yaml)
- OpenAPI 3.0 specification in YAML format

## Key Features Implemented

### 1. Custom API Information
- API title: "RuangKerja API"
- Description: "API documentation for RuangKerja application"
- Version: "v1.0.0"
- License: Apache 2.0

### 2. Example Endpoints with Documentation
- `GET /api/v1/hello` - Simple greeting endpoint
- `GET /api/v1/hello/{name}` - Personalized greeting with path parameter
- `GET /api/v1/health` - Health check endpoint

### 3. Swagger Annotations Used
- `@Tag` - Group related endpoints
- `@Operation` - Describe individual endpoints
- `@ApiResponses` & `@ApiResponse` - Document possible responses
- `@Parameter` - Document path/query parameters

## Next Steps

### Adding Documentation to Your Controllers
1. **Add @Tag annotation** at class level to group endpoints:
```java
@Tag(name = "Users", description = "User management operations")
@RestController
public class UserController {
    // controller methods
}
```

2. **Document your endpoints** with @Operation:
```java
@Operation(summary = "Get user by ID", description = "Retrieves a user by their unique identifier")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "User found"),
    @ApiResponse(responseCode = "404", description = "User not found")
})
@GetMapping("/users/{id}")
public User getUser(@Parameter(description = "User ID") @PathVariable Long id) {
    // method implementation
}
```

3. **Document request bodies** for POST/PUT endpoints:
```java
@PostMapping("/users")
public User createUser(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "User to create",
        content = @Content(schema = @Schema(implementation = User.class))
    )
    @RequestBody User user) {
    // method implementation
}
```

## Customization Options

### Additional Configuration Properties
You can add these to `application.properties` for further customization:

```properties
# Custom Swagger UI path
springdoc.swagger-ui.path=/custom-swagger

# Sort operations by HTTP method
springdoc.swagger-ui.operationsSorter=method

# Sort tags alphabetically
springdoc.swagger-ui.tagsSorter=alpha

# Disable try-it-out for all operations
springdoc.swagger-ui.tryItOutEnabled=false

# Enable/disable schema at the bottom
springdoc.swagger-ui.defaultModelsExpandDepth=-1
```

## Benefits of This Setup

1. **Automatic Documentation** - API documentation is generated automatically from your code
2. **Interactive Testing** - Test your API directly from the Swagger UI
3. **Standards Compliant** - Uses OpenAPI 3.0 specification
4. **Export Capabilities** - JSON/YAML formats can be imported to other tools
5. **Team Collaboration** - Provides a single source of truth for API documentation

## Reference Links
- [Baeldung Tutorial](https://www.baeldung.com/spring-rest-openapi-documentation)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI 3.0 Specification](https://swagger.io/specification/)
