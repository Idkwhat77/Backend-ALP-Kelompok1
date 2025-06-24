<div align="center">

# Backend - ALP Kelompok 1

**Challenge Based Learning Project Backend**

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)

*Interactive platform backend for addressing unemployment challenges in Indonesia*

</div>

## 🔗 Related Repository

**Frontend Repository:** [**Frontend-ALP-Kelompok1**](https://github.com/Idkwhat77/Frontend-ALP-Kelompok1)

---

## 📋 Overview

RESTful API backend built with Spring Boot, providing comprehensive server-side functionality for an employment platform. Features user management, job matching algorithms, and real-time data processing with MySQL database integration.

## ⚡ Quick Start

### Prerequisites
- [![Java 17+](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/downloads/)
- [![Maven 3.6+](https://img.shields.io/badge/Maven-3.6+-C71A36?style=flat&logo=apachemaven&logoColor=white)](https://maven.apache.org/download.cgi)
- [![MySQL 8.0+](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat&logo=mysql&logoColor=white)](https://dev.mysql.com/downloads/mysql/)
- [![XAMPP](https://img.shields.io/badge/XAMPP-recommended-FB7A24?style=flat&logo=xampp&logoColor=white)](https://www.apachefriends.org/download.html)

### Setup
```bash
# Clone repository
git clone https://github.com/Idkwhat77/Backend-ALP-Kelompok1.git

# Configure database
# Start XAMPP MySQL service
# Database 'ruangkerja' will be auto-created

# Run application
mvn spring-boot:run
```

**API Base URL:** `http://localhost:8080/api`

## 🏗️ Architecture

```
Backend Structure
├── Controllers     → REST API endpoints
├── Services       → Business logic layer
├── Repositories   → Data access layer
├── Models         → Entity definitions
└── Config         → Security & database setup
```

## 🔧 Key Features

| Feature | Description |
|---------|-------------|
| **Authentication** | JWT-based user authentication |
| **Job Management** | CRUD operations for job postings |
| **User Profiles** | Comprehensive user management |
| **Real-time Updates** | Live job matching notifications |
| **Data Analytics** | Employment statistics and insights |

## 👨‍💻 Development Team

| Name | Role |
|------|------|
| **M. Rifki Paranrengi** | **Team Lead & Backend** |
| **Aristo Benedict Iskandar** | **Backend Developer** |
| **Exsel Octaviand Gosal** | **Frontend Developer** |
| **Stella J. Chandra** | **Frontend Developer** |

## 🚨 Development Notes

> **✅ Project Completed:** This project has been completed as part of our ALP coursework. The API is fully functional and ready for deployment.

## 🔧 Troubleshooting

> [!WARNING]
> Before making any database changes, ensure you have backed up important data. Dropping the database will permanently delete all existing records.

### Common Issues

**XAMPP Not Running**
- **Problem:** Application fails to connect to database
- **Solution:** Ensure XAMPP is running and MySQL service is active
- **Check:** Verify MySQL is accessible at `localhost:3306`

**Database Schema Conflicts**
- **Problem:** Application fails to start due to column/table conflicts
- **Solution:** Drop the existing database and restart the application
- **Steps:**
  1. Delete the `ruangkerja` database from phpMyAdmin
  2. Restart the Spring Boot application
  3. The database will be automatically recreated with the updated schema

**Java Version Mismatch**
- **Problem:** Build fails or application won't start
- **Solution:** Ensure your Java version matches the one specified in `pom.xml`
- **Check:** Verify you're using Java 17+ as specified in the project configuration

> [!TIP]
> If you encounter persistent issues, try cleaning your Maven cache with `mvn clean install` and restart your IDE.
---

<div align="center">

**ALP Kelompok 1 Backend** • *Last Updated: June 2025*

</div>
