# ⚡ Instant Work — "Find work. Earn nearby."

> **Hyperlocal Micro-Task and Job Marketplace**  
> An academic final-year project engineered with Java (Spring Boot) and modern HTML5, CSS3, and JavaScript.

---

## 🌟 About Instant Work

**Instant Work** is a two-sided local micro-task marketplace designed to bridge the gap between people who need work done and nearby individuals seeking flexible, immediate earning opportunities.

Unlike traditional job portals that divide users into rigid "employer" or "worker" tiers, **every Instant Work account has dual capabilities**:
1. **Post Work**: Create a micro-task offering a fixed cash reward (₹), duration, schedule, and location.
2. **Find Work**: Discover nearby tasks within walking or commuting distance, accept them, execute them, and receive instant rewards into an integrated wallet.

---

## 🚀 Core Features

- 📍 **Hyperlocal Geolocation Engine**: Real-time Haversine distance calculation and filtering (within 1 km, 3 km, 5 km, 10 km).
- 🔄 **5-Stage Task Lifecycle**: `OPEN` $\rightarrow$ `ACCEPTED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED` $\rightarrow$ `PAYMENT_RELEASED`.
- 💰 **Simulated Wallet & Escrow Ledger**: Instant reward release from poster to worker's balance with immutable transaction logs.
- ⭐ **Mutual Reputation & Ratings**: 1-to-5 star rating and written reviews after every completed job.
- 👥 **Dual-Role Persona Switcher**: Instant switching between test personas (e.g. *Murali Sai - Task Poster* vs *Ravi Kumar - Worker*) to demonstrate the full two-sided workflow.
- 🛡️ **Admin Control Panel**: Real-time marketplace analytics, user trust verification toggles, and task moderation tools.
- 🗄️ **Embedded H2 SQL Console**: In-memory relational database ready for quick prototyping and direct SQL testing.

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Frontend** | HTML5, Vanilla CSS3 (Custom Design System), JavaScript (ES6+ Single Page App) |
| **Backend** | Java 21 LTS, Spring Boot 3.3.5 (Spring Web, Spring Data JPA, Hibernate, Validation) |
| **Database** | In-Memory H2 Database (zero setup required; seeds automatically on boot) |
| **Build Tool** | Apache Maven 3.9+ |

---

## 📋 Task Categories Supported

- 🍽️ **Restaurants** (Kitchen help, dinner rush service, counter support)
- 🏬 **Retail & Shops** (Sales assistant, inventory stocking, cashiering)
- 🎉 **Events** (Event coordination, symposium registration, ushering)
- 🏢 **Office** (Filing, document scanning, administrative assistance)
- 🏗️ **Construction** (Site assistance, material moving)
- 🚚 **Delivery** (Hyperlocal deliveries, field tasks)
- 💻 **IT & Software** (Computer setup, network verification, testing)
- ⌨️ **Data Entry** (Spreadsheet entry, invoice transcription)
- 🎓 **Education & Labs** (Lab setup, student tutoring)
- 📦 **Warehouse** (Boxing, packing, barcode labeling)
- 📈 **Sales & Marketing** (Flyer distribution, local promotion)
- 💼 **Other legitimate local micro-work**

---

## 🚦 Getting Started

### Prerequisites
- **Java 21 LTS** or higher
- **Maven 3.9+**

### 1. Clone the Repository
```bash
git clone https://github.com/MuraliSaiSure/Localjobs.git
cd Localjobs
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

### 3. Open in Browser
- **Application URL:** [http://localhost:8080](http://localhost:8080)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:instantworkdb`
  - User: `sa`
  - Password: *(blank)*

---

## 📡 REST API Documentation

### Task Endpoints (`/api/tasks`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tasks` | Discover tasks with query filters (`category`, `maxDistance`, `minReward`, `keyword`, `userLat`, `userLng`) |
| `GET` | `/api/tasks/{id}` | Get single task details |
| `POST` | `/api/tasks` | Post a new micro-task |
| `PUT` | `/api/tasks/{id}/accept?workerId={id}` | Accept a task (`OPEN` $\rightarrow$ `ACCEPTED`) |
| `PUT` | `/api/tasks/{id}/start?workerId={id}` | Start working (`ACCEPTED` $\rightarrow$ `IN_PROGRESS`) |
| `PUT` | `/api/tasks/{id}/complete?workerId={id}` | Mark completed (`IN_PROGRESS` $\rightarrow$ `COMPLETED`) |
| `PUT` | `/api/tasks/{id}/release-payment?posterId={id}` | Confirm & release reward (`COMPLETED` $\rightarrow$ `PAYMENT_RELEASED`) |
| `GET` | `/api/tasks/my-posted?userId={id}` | Tasks posted by user |
| `GET` | `/api/tasks/my-accepted?workerId={id}` | Tasks accepted by user |

### User & Wallet Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/users` | List all users (persona switching) |
| `GET` | `/api/users/{id}` | Get user profile and stats |
| `POST` | `/api/users/register` | Register new user account |
| `GET` | `/api/wallet/{userId}` | Get wallet balance and transactions |
| `POST` | `/api/ratings` | Submit 1–5 star rating and review |
| `GET` | `/api/admin/stats` | Platform metrics for Admin dashboard |

---

## 👥 Author
- **Murali Sai** — B.Tech Final Year Academic Project / Startup Prototype
