# Jubbisoft-Games-Store 🎮
🚀 Jubbisoft is a digital platform for video games, software, and community features. It allows users to buy, download, and play games, as well as access game updates, cloud saves, multiplayer, forums and game reviews. Built with **Spring Boot**, it supports secure authentication with Spring Security, REST API communication, and a microservice-based architecture.

⚠️ **Startup Order:**  

1️⃣ **Start Notice SVC first** – This microservice must be running before Jubbisoft.  
➡️ In the Notice SVC project, run the `Application` class.

2️⃣ **Start Jubbisoft (Main Application)** – Once Notice SVC is running, start the main project.  
➡️ In the Jubbisoft project, run the `Application` class.

3️⃣ Open [`http://localhost:8080/`](http://localhost:8080/) in **Google Chrome** – This is the entry point to the application.


## All Projects 🔗
- `[Jubbisoft Games Store (Main Application) link]`  https://github.com/jiponov/Jubbisoft-Games-Store
- `[Notice SVC (Microservice) link]`  https://github.com/jiponov/Notice-SVC

## Default Test Accounts for Jubbisoft 🧑‍💻
### **ADMINS (pre-added in DB)**
- `Username:` PlayaDeepCorporation  
  `Password:` 123123
- `Username:` JintenddoCorporation  
  `Password:` 123123
- `Username:` XlocksCorporation  
  `Password:` 123123

### **USERS (pre-added in DB)**
- `Username:` Lub123  
  `Password:` 123123
- `Username:` Jin123  
  `Password:` 123123
  
## Core Technologies Used 🛠
- **Backend:** Spring Framework - Spring Boot, Spring Security, Spring Data JPA, Hibernate. Object-Oriented Design and best practices for high-quality code.
- **Frontend:** Thymeleaf, HTML, CSS, JavaScript
- **Database:** MySQL 
- **Microservices:** REST API with Feign Clients for API communication - Notice SVC (the service includes its own database, separate and independent from the main project's database)
- **Security:** User and role management using standard Spring Security for authentication.
- **Data Validation and Error Handling:** Data validation across all application layers (DTO validation, business logic, entity constraints). Display appropriate validation messages to the user.
- **Scheduling:** Scheduled job that affects the application.
- **Testing:** Unit tests, integration tests, and API tests to ensure the functionality and reliability of the application (JUnit, Mockito).
- **Roles:** Admin, User

## Features 🚀
- **Game Library:** Browse, purchase, and manage digital games.
- **User Authentication:** Secure login system with role-based access.
- **Admin Panel:** Game management, user moderation, and financial reports.
- **Purchase System:** Buy games using in-app currency with transaction tracking.
- **Microservice Integration:** The Notice SVC handles notifications and license certificates.
- **Hybrid Storage System:** Supports both local server storage and external cloud-based game data and assets.
- **Thymeleaf Frontend:** Dynamic UI rendering.

## Disclaimer 📜
> **DISCLAIMER:** This project is created for educational purposes only. It does not represent a real digital store. No actual transactions or legal obligations exist.