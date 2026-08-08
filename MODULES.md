# Module Roadmap — "Jarvis" Project

Build strictly in this order. Do not move to the next module until the current one's acceptance criteria are all met.

---

## Module 0: Project Bootstrap
**Goal:** Skeleton that runs.
- Spring Boot 3 + Java 21 project (Maven), Dockerfile + docker-compose.yml
- Connect to Supabase Postgres (env vars for URL/key)
- Flyway configured, one dummy migration to prove it works
- Health check endpoint `GET /health`

**Done when:** `docker-compose up` runs the app, `/health` returns 200, migration applied on Supabase.

---

## Module 1: Auth & User Profile
**Goal:** Secure single-user access.
- Integrate Supabase Auth (JWT validation on backend)
- `User` entity synced from Supabase auth user
- All future endpoints require a valid JWT

**Done when:** You can log in via Supabase Auth (email/password or magic link) and hit a protected test endpoint successfully; unauthenticated requests get 401.

---

## Module 2: Timetable Management
**Goal:** System knows your weekly class schedule and free slots.
- `TimetableSlot` entity + CRUD endpoints
- Bulk-add endpoint (add whole week at once)
- Endpoint: `GET /timetable/free-slots?day=` returns free time blocks

**Done when:** You can input your full weekly college timetable and retrieve free slots per day.

---

## Module 3: Task & Priority Event Management
**Goal:** Add ad-hoc tasks and high-priority events (placement exams).
- `Task` CRUD
- `PriorityEvent` CRUD (name, date, type, optional JD text field for later)
- Endpoint: `GET /priority-events/upcoming`

**Done when:** You can add a task or a placement-exam event with a date and see it listed.

---

## Module 4: Topic Tracker + Spaced Repetition
**Goal:** Track subjects/topics and auto-schedule revisions.
- `Subject`, `Topic` CRUD
- "Mark topic studied/reviewed" endpoint that runs the SM-2-style calculation (ARCHITECTURE.md §6) and sets `next_revision_at`
- Endpoint: `GET /topics/due-for-revision`
- Unit tests for the spaced-repetition calculation (this is business-critical logic)

**Done when:** Marking a topic "reviewed" correctly pushes `next_revision_at` forward, and `due-for-revision` returns the right topics on the right days (verified via tests + manual check).

---

## Module 5: DSA Tracker (Excel Import)
**Goal:** Import your DSA question sheet and track progress.
- Excel import endpoint (Apache POI) — define the expected column format together before building (title, topic, difficulty, link, status)
- `DsaQuestion` CRUD + status update endpoint (solved / needs revision)
- Reuses the same spaced-repetition logic as Module 4 (extract it into a shared service if not already)
- Endpoint: `GET /dsa/today` → next N questions to solve + due revisions

**Done when:** You upload your real Excel sheet, it populates the DB correctly, and `/dsa/today` gives a sane daily list.

---

## Module 6: SQL & Aptitude Quota Tracker
**Goal:** Same pattern as DSA, simpler — daily quota tracking.
- `SqlQuestion`, `AptitudeQuestion` entities (can reuse a generic `PracticeQuestion` model if it fits — agent should propose this instead of duplicating code)
- `daily_quota_config` + `daily_progress` tracking
- Endpoint: `GET /practice/today-quota` → remaining SQL/DSA/Aptitude counts for today

**Done when:** The system correctly tracks "3/5 SQL done today" and resets appropriately at day boundary.

---

## Module 7: Scheduler Engine
**Goal:** The actual brain — combine everything into one daily plan.
- `SchedulerService` implementing the algorithm in ARCHITECTURE.md §6
- Endpoint: `GET /schedule/today` → structured JSON: time-blocked plan for the day
- Handles the overflow/carry-over case explicitly
- Full unit test suite: normal day, day with priority event, overloaded day

**Done when:** Given your real timetable + current due items + quotas, `/schedule/today` produces a plan you'd actually follow, including correct behavior when a placement event is added (slots get reprioritized).

---

## Module 8: Daily Briefing (LLM layer)
**Goal:** Turn the structured plan into a readable message.
- `BriefingService` calls a free-tier LLM API, given the JSON from `/schedule/today`, returns natural-language text
- Endpoint: `GET /briefing/today`
- **Phase 2 (optional, later):** JD parsing endpoint — paste a job description, extract topics to boost, feed into `PriorityEvent.boosted_topic_ids`

**Done when:** `/briefing/today` returns a clear, correct, non-hallucinated summary of your actual scheduled plan (spot-check against `/schedule/today` output).

---

## Module 9: Notifications
**Goal:** Get reminded without opening the app.
- Integrate OneSignal free tier (or email digest fallback)
- Scheduled job (Spring `@Scheduled`) sends the daily briefing each morning at a configured time
- Optional: reminder push when a revision becomes due mid-day

**Done when:** You receive an actual push/email on your phone with today's plan, automatically, without opening the app.

---

## Module 10: Frontend (Flutter)
**Goal:** Usable UI on iPhone + laptop.
- Flutter web app, installable as PWA on iPhone home screen
- Screens: Today's Plan, Timetable editor, Topics list, DSA/SQL/Aptitude trackers, Add Priority Event, Excel upload
- Calls the backend via REST with Supabase Auth token

**Done when:** You can do all daily actions (view plan, mark items done, add a task/event) from your phone home-screen app without touching the API directly.

---

## Suggested Milestone Checkpoints
- **Checkpoint A** (after Module 3): backend can store your full life — timetable, tasks, events
- **Checkpoint B** (after Module 6): backend tracks all learning data with spaced repetition
- **Checkpoint C** (after Module 8): backend can generate a real, correct daily plan and explain it in plain English
- **Checkpoint D** (after Module 10): fully usable end-to-end product on your phone
