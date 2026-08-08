# 🤖 JARVIS — AI-Powered Personal Study Assistant

> A full-stack, deterministic AI study manager designed to balance college timetables, spaced-repetition revisions, daily practice quotas, priority event deadlines, and dynamic study time-blocking.

---

## ✨ Features

- **🎓 5-Day Order College Timetable Integration**: Pre-seeded 5-day schedule (`DAY_1`–`DAY_5`, `HOLIDAY`) with an interactive Day Order switcher banner. College class hours are automatically removed from daily study time.
- **⚡ Deterministic Scheduler Engine**: 5-step time-blocking engine that schedules revision topics, practice quotas (DSA, SQL, Aptitude), priority exam prep, and tasks around college hours without overlapping.
- **🧠 Topics Tracker & Spaced Repetition (SM-2)**: Calculates intervals, repetitions, and ease factors per topic based on SuperMemo SM-2 algorithm.
- **📊 7-Column Excel & DSA Question Tracker**: Import spreadsheets (`Subject`, `Topic`, `Title`, `Problem #`, `Difficulty`, `Status`, `Question Link`) with automatic cascade resets and live status filters.
- **💬 Conversational AI Chat Assistant (JARVIS)**: Context-aware AI assistant leveraging Groq LLM (`llama-3.1-8b-instant`) with automatic fallback to clean deterministic responses when offline. Understands real-time local time, active Day Order, and remaining study blocks.
- **🔔 Desktop & Push Notifications**: Automatic reminders for morning briefings (08:00 AM), evening revisions (20:00 PM), and 60-second task start alerts with desktop notifications and audio chimes.
- **📊 KPI Dashboard Aggregation**: Real-time aggregated study time, solved question metrics, task completion progress, and revision queue size.
- **📱 Flutter Companion App**: Cross-platform Material 3 mobile application supporting Provider state management.

---

## 🛠️ Technology Stack

- **Backend**: Java 21, Spring Boot 3.3.5, Spring Security (Supabase OAuth2 / HS256 JWT validation), Spring Data JPA, Flyway (V1–V11)
- **Frontend**: Single Page Application with Vanilla JS, Glassmorphism CSS design system, HTML5 Web Audio & Notification APIs
- **Mobile**: Flutter 3 (Material 3, Provider pattern)
- **Database**: Supabase PostgreSQL 17
- **AI Integration**: Groq LLM API (`llama-3.1-8b-instant`)

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** & **Maven 3.8+**
- **Python 3** (for serving static frontend locally) or **Docker**

### Environment Configuration

Create a `.env` file in the root directory (or copy from `.env.example`):

```env
SUPABASE_DB_URL=jdbc:postgresql://<your-supabase-host>:5432/postgres?sslmode=require
SUPABASE_DB_USERNAME=<your_db_user>
SUPABASE_DB_PASSWORD=<your_db_password>
SUPABASE_JWT_SECRET=<your_jwt_secret>

LLM_API_KEY=<your_groq_api_key>
LLM_BASE_URL=https://api.groq.com/openai/v1
LLM_MODEL=llama-3.1-8b-instant
```

---

## 💻 Running the Application

### 1. Backend Server (Spring Boot on port 8080)

```bash
cd backend
mvn spring-boot:run
```

### 2. Frontend Dashboard (Web SPA on port 3000)

```bash
python3 -m http.server 3000 --directory frontend
```

Then open `http://localhost:3000` in your browser.

### 3. Docker Compose (Alternative)

```bash
docker-compose up --build
```

---

## 🧪 Running Unit Tests

```bash
cd backend
mvn test
```

> **All 52 unit tests passing (`BUILD SUCCESS`).**

---

## 📁 Repository Structure

```
├── backend/                  # Spring Boot 3.3.5 application (Java 21)
│   ├── src/main/java/        # Controllers, Services, Repositories, Entities
│   └── src/main/resources/   # application.yml, Flyway V1-V11 migrations
├── frontend/                 # Dark Glassmorphism Single Page Application
│   ├── index.html            # Dashboard structure & prompt chips
│   ├── app.js                # State management, API calls, desktop alerts
│   └── index.css             # Custom glassmorphism CSS design system
├── frontend/lib/             # Flutter Mobile Companion App codebase
├── docker-compose.yml        # Multi-container orchestration
├── ARCHITECTURE.md           # System architecture design document
├── PROGRESS.md               # Living project progress tracking
└── AGENT_RULES.md            # Agent developer rules & endpoints index
```

---

## 📄 License

This project is open-source under the MIT License.
