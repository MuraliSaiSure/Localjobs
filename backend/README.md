# ☕ LocalJobs — Backend API Service

Robust RESTful API backend engineered with **Java 21 LTS** and **Spring Boot 3.3.5**.

---

## 📁 Directory Structure
```
backend/
├── pom.xml          # Maven dependencies & build setup
└── src/
    └── main/
        ├── java/com/instantwork/
        │   ├── InstantWorkApplication.java
        │   ├── config/      # CORS & Pre-seeded DataInitializer
        │   ├── controller/  # REST Controllers (Tasks, Users, Wallet, Ratings, Admin)
        │   ├── dto/         # Request & Response Data Transfer Objects
        │   ├── model/       # JPA Entities (User, Task, Transaction, Review, Notification)
        │   ├── repository/  # Spring Data JPA Repositories
        │   └── service/     # Business logic, Haversine Geolocation, Payouts
        └── resources/
            └── application.properties # Server port 8080 & H2 DB configuration
```

---

## 🚀 How to Run Backend

```bash
cd backend
mvn spring-boot:run
```

- **Backend API Base:** `http://localhost:8080/api`
- **H2 SQL Database Console:** `http://localhost:8080/h2-console`
  - **JDBC URL:** `jdbc:h2:mem:localjobsdb`
  - **User:** `sa`
  - **Password:** *(blank)*
