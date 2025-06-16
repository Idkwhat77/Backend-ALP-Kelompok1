# ALP Final Project - Kelompok 1 (Backend)

[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)

This is the backend repository for a Challenge Based Learning (CBL) project focused on creating an interactive platform to reduce unemployment in Indonesia. The project implements the server-side logic and API for a full-stack web application using modern technologies and follows professional development practices.

> [!NOTE]
> This project is currently in development phase. Database configuration and deployment strategies may change for production environments.

## 🔗 Visit Frontend Repository 
You can find the frontend repository for this project here: 
[**Frontend-ALP-Kelompok1**](https://github.com/Idkwhat77/Frontend-ALP-Kelompok1)

## 👥 Team Members
- **Aristo Benedict Iskandar**
- **Exsel Octaviand Gosal**
- **M. Rifki Paranrengi**
- **Stella J. Chandra**


> [!IMPORTANT]
> As this is an academic project, development practices may vary as we learn and adapt. We strive to follow industry standards but acknowledge that consistency may improve over time as the team gains experience.

## 🛠️ Troubleshooting

> [!WARNING]
> Before making any database changes, ensure you have backed up important data. Dropping the database will permanently delete all existing records.

### Common Issues

#### XAMPP Not Running
- **Problem**: Application fails to connect to database
- **Solution**: Ensure XAMPP is running and MySQL service is active
- **Check**: Verify MySQL is accessible at `localhost:3306`

#### Database Schema Conflicts
- **Problem**: Application fails to start due to column/table conflicts
- **Solution**: Drop the existing database and restart the application
- **Steps**: 
  1. Delete the `ruangkerja` database from phpMyAdmin
  2. Restart the Spring Boot application
  3. The database will be automatically recreated with the updated schema

#### Java Version Mismatch
- **Problem**: Build fails or application won't start
- **Solution**: Ensure your Java version matches the one specified in `pom.xml`
- **Check**: Verify you're using Java 17+ as specified in the project configuration

> [!TIP]
> If you encounter persistent issues, try cleaning your Maven cache with `mvn clean install` and restart your IDE.

---
*Last updated: June 2025*
