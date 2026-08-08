# PROGRESS.md — Living Context File

> This file is the single source of truth for project state. Every agent session MUST read this file first, and MUST update it before ending any task or module. If this file is out of date, trust it less than the actual code — but always update it to match reality before stopping.

Last updated: 2026-08-08 15:35 IST

---

## Current Status
- **Current module in progress:** ALL 14 MODULES COMPLETE & VERIFIED ✅
- **Current Status:**
  - [x] **Render Free Tier Deployment Ready**: Multi-stage `Dockerfile`, `render.yaml` Blueprint, dynamic API URL discovery in `app.js`, and `${PORT:8080}` port binding created & verified.
  - [x] **GitHub Sync**: Pushed to [https://github.com/rishabh-nagar-9100/Personal-assistant-.git](https://github.com/rishabh-nagar-9100/Personal-assistant-.git).
  - [x] **5-Day Order College Timetable**: Full 5-day college schedule pre-seeded. Top banner prompt allows user to switch active Day Order (`DAY_1`–`DAY_5`, `HOLIDAY`).
  - [x] **Dynamic Study Allocation**: Removes exact college class hours and automatically packs overdue topic revisions (SM-2), practice questions (DSA, SQL, Aptitude), and tasks into remaining free windows.
  - [x] **Live Schedule & Interactive Task Tracking**: Live schedule displays colored course badges for college classes and interactive checkboxes for study tasks.
  - [x] **Real-time Task Start Notifications**: Dispatches push alerts via FCM and browser desktop notifications with audio chime.
  - [x] **Dynamic Subjects & 7-Column Excel Import**: 7-column Excel parser (`Subject`, `Topic`, `Problem Title`, `Problem #`, `Difficulty`, `Status`, `Question Link`).
  - [x] **Conversational AI Chat Assistant & Daily Briefing**: Groq LLM integration with real-time context aggregation and deterministic fallback.
  - [x] **Flyway Migrations V1–V11**: Applied cleanly to Supabase PostgreSQL.
  - [x] **Test Suite**: **52/52 unit tests passing** (`mvn test` $\rightarrow$ `BUILD SUCCESS`).
- **Running Servers:**
  - Backend: `http://localhost:8080` (Spring Boot 3.3.5, Java 21)
  - Web Dashboard: `http://localhost:3000` (Vanilla JS Dark Glassmorphism SPA)

---

## Module Completion Log

| Module | Status | Date Completed | Notes |
|---|---|---|---|
| 0 - Project Bootstrap | ✅ Done | 2026-08-07 | Spring Boot 3.3.5, Java 21, Maven. Flyway V1 migration applied. Health endpoint live. |
| 1 - Auth & User Profile | ✅ Done | 2026-08-07 | Spring Security + OAuth2 Resource Server for Supabase HS256 JWT validation. User entity synced from JWT claims on request. V2 migration applied. |
| 2 - Timetable Management | ✅ Done | 2026-08-07 | TimetableSlot CRUD, bulk weekly upload, free-slots computation (08:00-22:00 active range). Flyway V3 migration applied. |
| 3 - Task & Priority Events | ✅ Done | 2026-08-07 | Task CRUD, PriorityEvent CRUD + GET /priority-events/upcoming. Flyway V4 migration applied. |
| 4 - Topic Tracker + Spaced Repetition | ✅ Done | 2026-08-07 | Subject CRUD, Topic CRUD, SM-2 SpacedRepetitionCalculator, POST /topics/{id}/review, GET /topics/due-for-revision. Flyway V5 migration applied. |
| 5 - DSA Tracker | ✅ Done | 2026-08-07 | Apache POI Excel import (POST /dsa/import), DsaQuestion CRUD, SM-2 reviews, GET /dsa/today queue limit algorithm. Flyway V6 migration applied. |
| 6 - SQL & Aptitude Quota Tracker | ✅ Done | 2026-08-07 | Unified PracticeQuestion model (SQL/APTITUDE), DailyQuotaConfig, DailyProgress, GET /practice/today-quota. Flyway V7 migration applied. |
| 7 - Scheduler Engine | ✅ Done | 2026-08-08 | 5-step deterministic scheduling algorithm, GET /schedule/today, GET /schedule/date, time-blocking & carry-over overflow. |
| 8 - Daily Briefing (LLM) | ✅ Done | 2026-08-08 | OpenAI-compatible LlmClient, DailyBriefing caching (1 LLM call/day), GET /briefing/today, POST /briefing/today/regenerate, deterministic template fallback. Flyway V8 migration applied. |
| 9 - Notifications | ✅ Done | 2026-08-08 | NotificationToken, Notification entities, FCM registration, pending push payload query, Spring @Scheduled crons (08:00 AM & 20:00 PM). Flyway V9 migration applied. |
| 10 - Frontend (Flutter) | ✅ Done | 2026-08-08 | Full Material 3 Flutter application in `frontend/lib/` (Providers, ApiService, LoginScreen, HomeScreen, ScheduleScreen, TimetableScreen). |
| 11 - AI Chat Agent Backend | ✅ Done | 2026-08-08 | `ChatAgentService` (LLM intent parsing + deterministic fallback), `ChatAgentController` (POST /chat/message), `ChatRequest`/`ChatResponse` DTOs. Builds rich context from ALL backend services. |
| 12 - Dashboard Metrics & Frontend Redesign | ✅ Done | 2026-08-08 | `DashboardService` + `DashboardController` (GET /dashboard/metrics). Dark glassmorphism AI dashboard with left sidebar, KPI cards, Live Schedule timeline, Study Plan progress bars, AI Briefing, and embedded AI Chat Assistant panel. |
| 13 - Dynamic Subjects & 7-Col Excel Question Tracking | ✅ Done | 2026-08-08 | Flyway V10 migration. Dynamic Subject Tabs & Creation in Study Plan. 7-column Excel parser (`Subject`, `Topic`, `Title`, `Problem #`, `Difficulty`, `Status`, `URL`). Interactive Question Cards with problem badges and search filters. |
| 14 - 5-Day Order Timetable & Task Notifications | ✅ Done | 2026-08-08 | Flyway V11 migration (`timetable_slots.day_order`, `user_daily_state`). 5-day college schedule seeding, daily Day Order switcher banner (`DAY_1`–`DAY_5`, `HOLIDAY`), class removal, free study packing, interactive completion checkboxes, and desktop task start notifications. |

---

## Decisions Made So Far
- **Build tool:** Maven (not Gradle) — chosen per AGENT_RULES.md "pick one at project init and stay consistent".
- **Hibernate ddl-auto:** `validate` — Flyway owns all schema changes; Hibernate only validates.
- **Supabase JWT validation:** Uses `NimbusJwtDecoder` with `MacAlgorithm.HS256` and `SecretKeySpec` since Supabase Auth defaults to symmetric HS256 JWT signing secret.
- **Managed User Entity Requirement:** When creating JPA entities that reference `User`, always fetch managed entity via `userRepository.findById(user.getId()).orElse(user)` to avoid Hibernate `detached entity passed to persist` errors.
- **5-Day Order Timetable Mapping:**
  - Day 1: 12:30–14:15 (A), 16:00–16:50 (G), 16:50–18:10 (L11/L12)
  - Day 2: 08:00–09:40 (B), 09:45–11:30 (G), 11:35–12:25 (A)
  - Day 3: 12:30–14:15 (C), 14:20–15:10 (A), 15:10–16:00 (D), 16:00–16:50 (B)
  - Day 4: 08:00–09:40 (D), 09:45–10:35 (B), 11:35–12:25 (C)
  - Day 5: 14:20–15:10 (C), 16:00–16:50 (D)
  - Weekend / Holiday: 100% Free Study Day (14 hours free study blocks).
- **Deterministic Scheduler Engine:** Implemented `SchedulerService` following ARCHITECTURE.md §6: 1) gather free slots for active Day Order, 2) priority event prep boost (60 min slots), 3) overdue revisions, 4) remaining quotas, 5) carry-over overflow packing. College classes are tagged `COLLEGE_CLASS` and sorted chronologically with study items.
- **7-Column Excel Import Format:** Ingestion pipeline matches columns: `Subject`, `Topic`, `Problem Title`, `Problem #`, `Difficulty`, `Status`, `Question Link`.

---

## Environment / Setup Notes
- Supabase project: `ionxrrgxjpczdsveolos` (AP Northeast 2 — Seoul region, shared pooler)
- Required env vars (in `.env`, never committed):
  - `SUPABASE_DB_URL` — JDBC connection string with `?sslmode=require&prepareThreshold=0`
  - `SUPABASE_DB_USERNAME` — `postgres.ionxrrgxjpczdsveolos`
  - `SUPABASE_DB_PASSWORD` — Supabase DB password
  - `SUPABASE_JWT_SECRET` — Supabase JWT secret (Project Settings -> API -> JWT Secret)
  - `LLM_API_KEY` — Groq API key (optional, falls back to deterministic mode)
- How to run locally: 
  - Backend: `cd backend && mvn spring-boot:run` (port 8080)
  - Frontend: `python3 -m http.server 3000 --directory frontend` (port 3000)
  - Docker: `docker-compose up --build`
- Backend port: 8080
- Frontend port: 3000

---

## Next Action
**ALL FEATURES COMPLETED & OPERATIONAL.** Open `http://localhost:3000` to interact with the full JARVIS assistant:
- Top banner: select today's Day Order (`Day 1` to `Day 5` or `Holiday`).
- Live Schedule: view college classes + study tasks, and check off study tasks to update completion progress in real time.
- Study Plan: upload 7-column Excel sheets for any subject.
- AI Assistant: chat with JARVIS for instant schedule summaries and recommendations.
