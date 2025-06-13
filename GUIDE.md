# 🚀 RuangKerja - Setup & Development Guide

[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)

## 📋 Prerequisites

Before running this project, ensure you have the following installed:

### Required Software
- **Java JDK 17+** - [Download here](https://openjdk.org/)
- **Maven 3.8+** - [Installation guide](https://maven.apache.org/install.html)
- **XAMPP** - [Download here](https://www.apachefriends.org/)
- **Git** - [Download here](https://git-scm.com/)

### Verify Installation
```bash
java -version
mvn -version
git --version
```

## 🔧 Backend Setup

### 1. Clone the Repository
```bash
git clone https://github.com/Idkwhat77/Backend-ALP-Kelompok1.git
cd Backend-ALP-Kelompok1
```

### 2. Database Setup
1. **Start XAMPP Control Panel**
2. **Start Apache and MySQL services**

> [!NOTE]
> No need to manually create the database! Spring Boot will automatically create the `ruangkerja` database when the application starts if it doesn't exist.

### 3. Configure Database Connection
Edit `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/ruangkerja?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Server Configuration
server.port=8080
```

### 4. Build & Run the Backend
```bash
# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

**Alternative run methods:**
```bash
# Using Maven wrapper (if available)
./mvnw spring-boot:run

# Or build JAR and run
mvn clean package
java -jar target/ruangkerja-backend-*.jar
```

### 5. Verify Backend is Running
- **API Base URL:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **API Documentation:** `http://localhost:8080/api-docs`

## 🌐 Frontend Setup

### 1. Navigate to Frontend Directory
```bash
cd ../Frontend-ALP-Kelompok1
```

### 2. Serve Frontend Files
**Option A: Using Live Server (Recommended for development)**
- Install [Live Server extension](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer) in VS Code
- Right-click on `index.html` → "Open with Live Server"
- Access at `http://127.0.0.1:5500`

**Option B: Using Python HTTP Server**
```bash
# Python 3
python -m http.server 8000

# Python 2 (if needed)
python -m SimpleHTTPServer 8000
```

**Option C: Using Node.js**
```bash
npx http-server -p 8000
```

## 🔗 Full Stack Integration

### Test API Integration
1. **Backend running on:** `http://localhost:8080`
2. **Frontend running on:** `http://127.0.0.1:5500` (or your chosen port)
3. **Test pages available in:** [`api/`](c:\Users\rifki\Desktop\Final ASLI\Frontend-ALP-Kelompok1\api/) directory

## 🧪 Testing

### Backend API Testing
```bash
# Test user registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","username":"testuser","email":"test@example.com","password":"password123"}'

# Test user login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"test@example.com","password":"password123"}'

# Test get current user
curl -X GET "http://localhost:8080/api/auth/me?userId=1"
```

## 🛠️ Development Workflow

### Making Changes
1. **Backend changes:** Restart Spring Boot application
2. **Frontend changes:** Refresh browser (or auto-reload with Live Server)
3. **Database changes:** Check `application.properties` for auto-update settings

## 🐛 Troubleshooting

### Common Issues

**Backend won't start:**
```bash
# Check if port 8080 is in use
netstat -an | findstr 8080

# Kill process if needed (Windows)
taskkill /F /PID <process-id>
```

**Database connection errors:**
- Ensure XAMPP MySQL is running
- Check database name and credentials
- Verify MySQL port (default: 3306)

**CORS errors in frontend:**
- Backend includes CORS configuration in [`CorsConfig.java`](c:\Users\rifki\Desktop\Final ASLI\Backend-ALP-Kelompok1\src\main\java\com\ruangkerja\rest\config\CorsConfig.java)
- Ensure frontend URL is allowed in CORS settings

**Maven build errors:**
```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Reinstall dependencies
mvn clean install
```

## 📚 Additional Resources

- **Spring Boot Documentation:** [spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **Tailwind CSS Documentation:** [tailwindcss.com](https://tailwindcss.com/)
- **MySQL Documentation:** [dev.mysql.com/doc/](https://dev.mysql.com/doc/)

## 👥 Team Members
- **Aristo Benedict Iskandar** - Backend Developer
- **Exsel Octaviand Gosal** - Frontend Developer  
- **M. Rifki Paranrengi** - Backend Developer
- **Stella J. Chandra** - Frontend Developer

---

> [!TIP]
> For development, keep both backend and frontend running simultaneously for the best experience.

> [!NOTE]
> This is an academic project. Configuration may change for production deployment.

*Last updated: June 2025*