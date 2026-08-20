# ⚡ LocalJobs — "Find work. Earn nearby."

> **Hyperlocal Micro-Task and Job Marketplace**  
> An academic final-year project engineered with a modular **Frontend** (HTML5, Vanilla CSS3, JS with Vercel support) and **Backend** (Java 21 LTS, Spring Boot 3.3.5, H2 Database).

---

## 🌟 About LocalJobs

**LocalJobs** is a two-sided local micro-task marketplace designed to bridge the gap between people who need work done and nearby individuals seeking flexible, immediate earning opportunities.

Unlike traditional job portals that divide users into rigid "employer" or "worker" tiers, **every LocalJobs account has dual capabilities**:
1. **Post Work**: Create a micro-task offering a fixed cash reward (₹), duration, schedule, and location.
2. **Find Work**: Discover nearby tasks within walking or commuting distance, accept them, execute them, and receive instant rewards into an integrated wallet.

---

## 📁 Modular Project Structure

The project is structured into two clean, self-contained directories:

```
Localjobs/
├── 🎨 frontend/                  # Standalone Frontend Application (SPA)
│   ├── index.html               # Main application layout & modals
│   ├── css/
│   │   └── style.css            # Indigo-Teal design system & responsive rules
│   ├── js/
│   │   ├── api.js               # REST client with dynamic API_BASE detection
│   │   ├── state.js             # Central reactive state & persona switcher
│   │   └── app.js               # UI rendering & 5-stage lifecycle actions
│   ├── vercel.json              # 1-Click Vercel deployment configuration
│   ├── package.json             # Optional local serve scripts
│   └── README.md                # Frontend documentation
│
├── ☕ backend/                   # Spring Boot 3.3.5 REST API Service
│   ├── pom.xml                  # Maven dependencies & build configuration
│   ├── src/main/java/           # Java 21 controllers, services, entities, DTOs
│   ├── src/main/resources/      # application.properties & database setup
│   └── README.md                # Backend documentation
│
├── test_suite.ps1               # Automated end-to-end integration test runner
├── .gitignore
└── README.md
```

---

## 🎨 Advanced Design System & Colors

- **Primary Accent**: Electric Indigo & Royal Violet (`#4F46E5` / `#6366F1`)
- **Secondary Accent**: Oceanic Cyan & Teal (`#06B6D4` / `#0EA5E9`)
- **Success & Wallet Payouts**: Emerald Green (`#10B981`)
- **Reputation & Ratings**: Warm Sunset Gold (`#F59E0B`)
- **Surfaces**: Frosted glassmorphism, elevated soft shadows, and rounded cards (`Plus Jakarta Sans` typography).

---

## 🚀 How to Run Locally

### 1. Start the Backend API (Port 8080)
```bash
cd backend
mvn spring-boot:run
```
- Backend REST API: [http://localhost:8080/api](http://localhost:8080/api)
- H2 SQL Database Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:localjobsdb`, user: `sa`, password: blank)

### 2. Launch the Frontend
You can run the frontend in any of the following ways:
- **Option A (Direct in Browser):** Double-click `frontend/index.html`
- **Option B (Local Dev Server):**
  ```bash
  cd frontend
  npx serve -l 3000 .
  ```
  Open [http://localhost:3000](http://localhost:3000) (the frontend automatically connects to the backend at `http://localhost:8080`).
- **Option C (Embedded in Spring Boot):** Open [http://localhost:8080](http://localhost:8080)

---

## ☁️ Deployment (Vercel & Cloud)

### Frontend on Vercel:
1. Push this repository to GitHub.
2. In [Vercel Dashboard](https://vercel.com), import the `Localjobs` repository.
3. In **Project Settings**, set **Root Directory** to `frontend`.
4. Deploy! The `vercel.json` ensures all routes route cleanly to `index.html`.

---

## 📡 REST API Endpoints Overview

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/tasks` | Discover tasks with query filters (`category`, `maxDistance`, `minReward`, `keyword`, `userLat`, `userLng`) |
| `POST` | `/api/tasks` | Post a new micro-task |
| `PUT` | `/api/tasks/{id}/accept?workerId={id}` | Accept task (`OPEN` $\rightarrow$ `ACCEPTED`) |
| `PUT` | `/api/tasks/{id}/start?workerId={id}` | Start task (`ACCEPTED` $\rightarrow$ `IN_PROGRESS`) |
| `PUT` | `/api/tasks/{id}/complete?workerId={id}` | Mark completed (`IN_PROGRESS` $\rightarrow$ `COMPLETED`) |
| `PUT` | `/api/tasks/{id}/release-payment?posterId={id}` | Release reward to worker wallet |
| `GET` | `/api/wallet/{userId}` | Get user wallet balance & transaction ledger |
| `POST` | `/api/ratings` | Submit 1–5 star rating & review |
| `GET` | `/api/admin/stats` | Platform statistics for Admin Dashboard |
| `PUT` | `/api/admin/users/{id}/toggle-verify` | Toggle verified user badge |
| `DELETE` | `/api/admin/tasks/{id}` | Delete / moderate spam task |

---

## 👥 Author
- **Murali Sai** — B.Tech Final Year Academic Project / Startup Prototype
