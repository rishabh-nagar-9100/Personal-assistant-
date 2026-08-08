# AGENTS.md — Workspace-Level Agent Rules & Continuation Guide

> **CRITICAL: Read PROGRESS.md first, then this file, then ARCHITECTURE.md before doing ANYTHING.**

---

## 1. Project Overview & Current State
"Jarvis" is a **single-user AI-powered personal study assistant** featuring:
- **Backend:** Java 21, Spring Boot 3.3.5 on port 8080 (`mvn spring-boot:run` or `docker-compose up`)
- **Frontend:** Vanilla JS / Glassmorphism Single Page Application on port 3000 (`python3 -m http.server 3000 --directory frontend`)
- **Database:** Supabase PostgreSQL 17 via Flyway migrations (V1–V11) with `hibernate.ddl-auto=validate`
- **Test Suite:** **52/52 unit tests passing** (`mvn test` $\rightarrow$ `BUILD SUCCESS`)

---

## 2. All 14 Completed Modules Summary

1. **Module 0: Project Bootstrap** — Spring Boot 3.3.5, Java 21, Maven, Flyway V1, `/health` endpoint.
2. **Module 1: Auth & User Profile** — Spring Security + OAuth2 Resource Server for Supabase HS256 JWT validation, user auto-sync.
3. **Module 2: Timetable Management** — Timetable slots, bulk upload, free slot calculator (08:00–22:00 active hours).
4. **Module 3: Tasks & Priority Events** — Task CRUD (`PENDING`/`DONE`), Priority events (exams/placements), upcoming deadlines query.
5. **Module 4: Topics Tracker & Spaced Repetition** — Subjects & Topics hierarchy, SuperMemo SM-2 calculator (`interval`, `repetitions`, `easeFactor`).
6. **Module 5: DSA Problem Tracker** — DSA questions, Apache POI Excel import, SM-2 reviews, daily queue limits.
7. **Module 6: SQL & Aptitude Daily Quota Tracker** — Unified `practice_questions` table (`category_type`), daily quota config (`dsa`, `sql`, `aptitude`), daily progress tracker.
8. **Module 7: Deterministic Scheduler Engine** — 5-step conflict-free time-blocking engine (`/schedule/today`, `/schedule/date`) combining priority prep, revisions, quotas, tasks, and college classes.
9. **Module 8: Daily Briefing (LLM)** — OpenAI-compatible `LlmClient` for Groq (`llama-3.1-8b-instant`), daily briefing caching (1 LLM call/day), deterministic fallback.
10. **Module 9: Notifications & Cron Schedulers** — Firebase Cloud Messaging (FCM) tokens, 08:00 AM Morning Briefing alerts, 20:00 PM Evening Revision alerts, 60-second live task start reminders.
11. **Module 10: Flutter Mobile Companion App** — Material 3 mobile app in `frontend/lib/` with Providers.
12. **Module 11: Conversational AI Chat Agent** — `ChatAgentService` (POST `/chat/message`) aggregating real-time context from all backend services with Groq LLM + deterministic fallback.
13. **Module 12: Dashboard Metrics & KPI Aggregation** — `DashboardService` (GET `/dashboard/metrics`) aggregating study time, tasks completed, revision queue, quota targets.
14. **Module 13: Dynamic Subjects & 7-Column Excel Import** — Dynamic subject management, 7-column Excel parser (`Subject`, `Topic`, `Title`, `Problem #`, `Difficulty`, `Status`, `URL`), cascade resets (`DELETE /subjects/all`, `DELETE /practice/clear-all`, `DELETE /dsa/all`).
15. **Module 14: 5-Day College Timetable, Study Allocation & Task Notifications** — Pre-seeded 5-day college schedule (`DAY_1`–`DAY_5`, `HOLIDAY`), daily Day Order selector, class time removal, interactive schedule task checkboxes with sound alerts, real-time desktop notifications.

---

## 3. Key Backend Method Signatures (CRITICAL — Avoid Name Mismatches)

Use these **exact signatures** when integrating backend services:

| Service | Method | Returns |
|---|---|---|
| `SchedulerService` | `generateTodaySchedule(User user)` | `DailyScheduleResponse` |
| `SchedulerService` | `generateScheduleForDate(User user, LocalDate date)` | `DailyScheduleResponse` |
| `TimetableService` | `getFreeSlotsForDayOrder(User user, String dayOrder, DayOfWeek day)` | `List<FreeSlotResponse>` |
| `TimetableService` | `getSlotsForDayOrder(User user, String dayOrder)` | `List<SlotResponse>` |
| `TimetableService` | `getActiveDayOrder(User user, LocalDate date)` | `String` |
| `TimetableService` | `setActiveDayOrder(User user, LocalDate date, String dayOrder)` | `String` |
| `TimetableService` | `seedCollegeTimetable(User user)` | `List<SlotResponse>` |
| `TopicService` | `getTopicsDueForRevision(User user)` | `List<TopicResponse>` |
| `PracticeService` | `getTodayQuotaStatus(User user)` | `TodayQuotaResponse` |
| `PracticeService` | `getQuotaConfig(User user)` | `QuotaConfigResponse` |
| `TaskService` | `getUserTasks(User user, TaskStatus status)` | `List<TaskResponse>` |
| `UserService` | `getOrCreateUser(JwtAuthenticationToken authToken)` | `User` |
| `BriefingService` | `getTodayBriefing(User user)` | `DailyBriefingResponse` |
| `ChatAgentService` | `processChatMessage(User user, String message)` | `ChatResponse` |
| `DashboardService` | `getDashboardMetrics(User user)` | `DashboardMetricsResponse` |

---

## 4. Key Rules & Coding Standards

1. **Managed User Entity Rule (⚠️ High Priority):**
   When saving new JPA entities referencing `User`, always fetch managed entity first:
   `userRepository.findById(user.getId()).orElse(user)`
   Passing detached `User` directly to `new SomeEntity(user, ...)` causes Hibernate `detached entity passed to persist` errors.
2. **Deterministic Scheduling Boundary:**
   LLMs never compute raw schedules, time blocks, or SM-2 intervals. All scheduling is deterministic Java code in `SchedulerService` and `SpacedRepetitionCalculator`.
3. **LLM Cost & Fallback Discipline:**
   When `LLM_API_KEY` is not set or API fails, services (`BriefingService`, `ChatAgentService`) fall back to clean deterministic template responses without crashing.
4. **Flyway Migrations:**
   All database schema changes go through `backend/src/main/resources/db/migration/V<n>__<name>.sql`. Currently on **V11**.
5. **Excel Header Detection:**
   In `DocumentParserService`, match `"Question Link"` / `"url"` / `"source"` **before** general `"question"` keyword checks so column 7 is parsed as `source_link` and column 3 is extracted as `title`.

---

## 5. Complete REST API Endpoint Directory

```
GET  /health                                 — Public health check
GET  /me                                     — User profile from JWT

POST /timetable                              — Create timetable slot
POST /timetable/bulk                         — Bulk create slots
GET  /timetable                              — List all slots
GET  /timetable/day/{day}                    — Slots for a day
DELETE /timetable/{id}                       — Delete slot
GET  /timetable/free-slots?day={day}         — Free time windows
POST /timetable/seed-college                 — Seed 5-day college schedule
GET  /timetable/day-order/current            — Today's active Day Order & classes
POST /timetable/day-order/select             — Select today's Day Order (DAY_1..5, HOLIDAY)
GET  /timetable/day-order/slots?dayOrder=    — Class slots for Day Order
GET  /timetable/day-order/free-slots?dayOrder= — Free study windows for Day Order

POST /tasks                                  — Create task
GET  /tasks                                  — List tasks (?status=PENDING|DONE)
PATCH /tasks/{id}/status                     — Update task status
DELETE /tasks/{id}                           — Delete task

POST /priority-events                        — Create priority event
GET  /priority-events                        — List all
GET  /priority-events/upcoming               — Upcoming events
DELETE /priority-events/{id}                 — Delete event

POST /subjects                               — Create subject
GET  /subjects                               — List subjects
DELETE /subjects/{id}                        — Delete subject & its questions
DELETE /subjects/all                         — Wipe all subjects & questions

POST /topics                                 — Create topic
GET  /topics/subject/{subjectId}             — Topics by subject
POST /topics/{id}/review                     — Review topic (SM-2)
GET  /topics/due-for-revision                — Due topics
DELETE /topics/{id}                          — Delete topic

POST /dsa/import                             — Upload Excel (.xlsx) or PDF
POST /dsa                                    — Create DSA question
GET  /dsa                                    — List all DSA questions
POST /dsa/{id}/review                        — Review DSA question
GET  /dsa/today?limit=5                      — Today's DSA queue
DELETE /dsa/{id}                             — Delete DSA question
DELETE /dsa/all                              — Delete all DSA questions

GET  /practice/quota-config                  — Get quota settings
PUT  /practice/quota-config                  — Update quota settings
GET  /practice/today-quota                   — Today's progress
POST /practice/questions                     — Create practice question
GET  /practice/questions?categoryType        — List by category
POST /practice/questions/{id}/review         — Review practice question
DELETE /practice/questions/{id}              — Delete practice question
DELETE /practice/clear-all                   — Wipe all practice questions

GET  /schedule/today                         — Today's time-blocked schedule
GET  /schedule/date?date=YYYY-MM-DD          — Schedule for specific date

GET  /briefing/today                         — Today's AI briefing
POST /briefing/today/regenerate              — Force regenerate briefing

POST /notifications/fcm-token                — Register FCM token
GET  /notifications/pending                  — Unread notifications
GET  /notifications                          — All notifications
POST /notifications/{id}/read                — Mark as read

GET  /dashboard/metrics                      — KPI metrics aggregation
POST /chat/message                           — AI conversational assistant
```

---

## 6. How to Run Locally

```bash
# Backend (Spring Boot on port 8080)
cd backend && mvn spring-boot:run

# Frontend (Web Dashboard on port 3000)
python3 -m http.server 3000 --directory frontend

# Or via Docker:
docker-compose up --build
```
