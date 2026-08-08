# Project: "Jarvis" — Personal Study & Executive AI Assistant

## 1. Product Vision & Overview
**Jarvis** is a single-user, always-available AI-powered executive study and placement assistant designed to streamline college coursework, technical interview preparation (DSA, SQL, Aptitude), and priority milestone tracking.

### Core Capabilities:
- **Timetable & Free Slot Calculator**: Ingests college class timetables and calculates exact free study blocks.
- **Subject & Topic Spaced Repetition (SM-2)**: Manages subjects and topics with modified SuperMemo SM-2 spaced repetition intervals.
- **Dynamic Subject Management & 7-Column Excel Tracking**: Ingests 7-column spreadsheets (`Subject`, `Topic`, `Problem Title`, `Problem #`, `Difficulty`, `Status`, `Question Link`) for any subject with glowing `#number` badges and real-time status selectors.
- **Daily Quotas & Category Queue**: Tracks daily targets and completion for DSA, SQL, and Aptitude practice.
- **Deterministic Scheduling Engine**: Automatically builds conflict-free, time-blocked daily schedules prioritizing exams and overdue revisions.
- **Interactive Conversational AI Assistant**: Conversational AI chatbot powered by Groq (`llama-3.1-8b-instant`) with deep context injection across all backend modules and deterministic fallback.
- **LLM Daily Briefings & Summaries**: Morning executive briefings generated via LLM (or deterministic templates if offline).
- **Automated Notifications**: Firebase Cloud Messaging (FCM) push notifications and scheduled cron reminders.

---

## 2. Architectural Principles & Constraints
1. **Single-User Scope**: Dedicated personal assistant architecture with Supabase HS256 JWT authentication.
2. **$0 Infrastructure Budget**: Built entirely on free-tier services (Supabase PostgreSQL, Render/Railway container hosting, Groq free-tier LLM, Firebase Cloud Messaging).
3. **Deterministic Math vs. Generative AI**:
   - **Scheduling and SM-2 Spaced Repetition Math are 100% deterministic** in Java.
   - **LLM is used strictly for conversational assistance and natural language summarization**, never for time slot calculations or priority weights.
4. **Dual Frontend Clients**:
   - **Primary Web Dashboard & SPA** (`frontend/index.html`, `frontend/app.js`, `frontend/style.css`): Modern glassmorphism UI with responsive desktop/mobile support, sidebar navigation, KPI cards, live timeline, and embedded AI chat.
   - **Secondary Companion Client** (`frontend/lib/`): Flutter cross-platform mobile client.
5. **Robust Database Migrations**: Supabase PostgreSQL managed via Flyway migrations (`V1`–`V10`) with `validateOnMigrate` and clean JPA entity lifecycle management.

---

## 3. High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CLIENT APPLICATIONS                           │
│  ┌─────────────────────────────────────┐  ┌───────────────────────────┐ │
│  │   Web Dashboard & SPA (Port 3000)   │  │   Flutter Mobile App      │ │
│  │   (Vanilla JS + Glassmorphism CSS)  │  │   (Dart / Flutter PWA)    │ │
│  └──────────────────┬──────────────────┘  └─────────────┬─────────────┘ │
└─────────────────────┼───────────────────────────────────┼───────────────┘
                      │  HTTPS / REST (Supabase JWT Bearer Auth)
                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                SPRING BOOT 3.3.5 BACKEND (Port 8080)                    │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │ REST CONTROLLER LAYER                                             │  │
│  │  • Auth (/me, /health)           • Timetable (/timetable)         │  │
│  │  • Subjects & Topics (/subjects) • Tasks (/tasks, /priority-event)│  │
│  │  • DSA & Practice (/dsa, /prac)  • Scheduler (/schedule)          │  │
│  │  • Daily Briefing (/briefing)    • Notifications (/notifications) │  │
│  │  • Dashboard Metrics (/dashboard)• AI Chat Agent (/chat/message)  │  │
│  └─────────────────────────────────┬─────────────────────────────────┘  │
│                                    │                                    │
│  ┌─────────────────────────────────▼─────────────────────────────────┐  │
│  │ SERVICE & DOMAIN LAYER                                            │  │
│  │  • SchedulerService (Deterministic time-blocking algorithm)       │  │
│  │  • SpacedRepetitionCalculator (SM-2 review interval engine)       │  │
│  │  • DocumentParserService (Apache POI 7-col Excel & PDF parser)    │  │
│  │  • ChatAgentService (Multi-module context aggregator & chatbot)   │  │
│  │  • BriefingService (Morning executive briefing synthesizer)       │  │
│  │  • DashboardService (KPI metric calculation & progress aggregator)│  │
│  │  • NotificationService & CronScheduler (FCM push triggers)        │  │
│  └─────────────────────────────────┬─────────────────────────────────┘  │
│                                    │ Spring Data JPA / Hibernate        │
│  ┌─────────────────────────────────▼─────────────────────────────────┐  │
│  │ REPOSITORY LAYER (13 JPA Repositories)                            │  │
│  └─────────────────────────────────┬─────────────────────────────────┘  │
└────────────────────────────────────┼────────────────────────────────────┘
                                     │ JDBC Connection Pool (HikariCP)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     SUPABASE POSTGRESQL DATABASE                        │
│  • Flyway Migrations (V1 to V10)                                        │
│  • 12 Relational Tables (Users, Timetable, Subjects, Topics, Practice,  │
│    DSA, Tasks, Priority Events, Quotas, Progress, Briefings, Tokens)    │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │
         ┌───────────────────────────┴───────────────────────────┐
         ▼                                                       ▼
┌────────────────────────────────┐              ┌────────────────────────────────┐
│      GROQ LLM API CLOUD        │              │  FIREBASE CLOUD MESSAGING      │
│  • Model: llama-3.1-8b-instant │              │  • FCM Push Notifications      │
│  • Briefings & AI Chat Agent   │              │  • Scheduled Cron Reminders    │
│  • Deterministic fallback mode │              │  • Background Study Alerts     │
└────────────────────────────────┘              └────────────────────────────────┘
```

---

## 4. Package Structure & Module Map

```
com.jarvis
├── JarvisApplication.java
├── auth/                 # User entity, Supabase JWT verification, SecurityConfig
├── briefing/             # Daily briefings, Groq LLM integration, fallback templates
├── chat/                 # AI Chat Agent service, context aggregation, REST controller
├── dashboard/            # KPI metrics aggregation, completion rates, progress engine
├── dsa/                  # DSA questions, Apache POI 7-col Excel parser, PDF extractor
├── health/               # Public health check endpoint (/health)
├── notification/         # FCM push notifications, device tokens, scheduled cron alerts
├── practice/             # Practice questions (DSA, SQL, Aptitude), quota configs & progress
├── scheduler/            # Deterministic conflict-free daily time-blocking engine
├── spacedrepetition/     # SuperMemo SM-2 spaced repetition calculator & quality ratings
├── task/                 # Tasks (Pending/Done) and Priority Events (Exams/Deadlines)
├── timetable/            # College class schedule, commitments, free-slot calculator
└── topic/                # Subject hierarchy, topics, syllabus items, cascade resets
```

---

## 5. Database Schema & Flyway Evolution

The relational database schema is version-controlled via Flyway migrations in `backend/src/main/resources/db/migration/`:

```
V1__init_schema.sql                       → Initial 11 tables & enums
V2__seed_demo_data.sql                    → Seed timetable & initial practice items
V3__fix_user_id.sql                       → Standardize UUID user_id mapping
V4__add_fcm_tokens.sql                    → FCM push notification tokens table
V5__add_briefing_table.sql                → Daily briefing persistent history
V6__add_category_type_to_practice_questions.sql → Support DSA / SQL / Aptitude categories
V7__dsa_sub_category.sql                  → Topic categorisation for DSA questions
V8__fix_task_status_enum.sql              → Task status enum compatibility
V9__sync_enums_and_triggers.sql           → Synchronize Postgres enums and updated_at triggers
V10__add_subject_question_tracking_fields.sql → Add subject_id, problem_number, source_link
```

### Relational Schema Summary:

```sql
users (
  id UUID PRIMARY KEY,
  email VARCHAR UNIQUE NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

timetable_slots (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  day_of_week VARCHAR(10) NOT NULL, -- MONDAY..SUNDAY
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  type VARCHAR(30) NOT NULL,        -- CLASS, FREE, FIXED_COMMITMENT
  label VARCHAR(100) NOT NULL
);

subjects (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  name VARCHAR(100) NOT NULL
);

topics (
  id UUID PRIMARY KEY,
  subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
  name VARCHAR(150) NOT NULL,
  status VARCHAR(30) DEFAULT 'NOT_STARTED', -- NOT_STARTED, IN_PROGRESS, COMPLETED
  last_studied_at TIMESTAMP WITH TIME ZONE,
  next_revision_at DATE,
  ease_factor DOUBLE PRECISION DEFAULT 2.5,
  interval_days INT DEFAULT 1,
  repetition_count INT DEFAULT 0
);

tasks (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  title VARCHAR(200) NOT NULL,
  description TEXT,
  due_date DATE,
  priority VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH
  status VARCHAR(20) DEFAULT 'PENDING',  -- PENDING, DONE
  linked_topic_id UUID
);

priority_events (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  name VARCHAR(150) NOT NULL,
  event_date DATE NOT NULL,
  type VARCHAR(30) NOT NULL,             -- PLACEMENT_TEST, EXAM, INTERVIEW, DEADLINE
  jd_text TEXT,
  boosted_topic_ids TEXT[]
);

practice_questions (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  category_type VARCHAR(20) NOT NULL,    -- DSA, SQL, APTITUDE
  sub_category VARCHAR(100),
  title VARCHAR(255) NOT NULL,
  problem_number VARCHAR(50),            -- e.g. #1, #121, #217
  difficulty VARCHAR(20) DEFAULT 'MEDIUM',-- EASY, MEDIUM, HARD
  status VARCHAR(30) DEFAULT 'NOT_STARTED',-- NOT_STARTED, IN_PROGRESS, SOLVED, NEEDS_REVISION
  source_link TEXT,                      -- LeetCode / problem URL
  subject_id UUID REFERENCES subjects(id),
  subject_name VARCHAR(100),
  last_attempted_at TIMESTAMP WITH TIME ZONE,
  next_revision_at DATE,
  ease_factor DOUBLE PRECISION DEFAULT 2.5,
  interval_days INT DEFAULT 1,
  repetition_count INT DEFAULT 0
);

dsa_questions (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  title VARCHAR(255) NOT NULL,
  topic VARCHAR(100) NOT NULL,
  problem_number VARCHAR(50),
  difficulty VARCHAR(20) DEFAULT 'MEDIUM',
  status VARCHAR(30) DEFAULT 'NOT_STARTED',
  source_link TEXT,
  subject_id UUID REFERENCES subjects(id),
  last_attempted_at TIMESTAMP WITH TIME ZONE,
  next_revision_at DATE,
  ease_factor DOUBLE PRECISION DEFAULT 2.5,
  interval_days INT DEFAULT 1,
  repetition_count INT DEFAULT 0
);

daily_quota_config (
  user_id UUID PRIMARY KEY REFERENCES users(id),
  dsa_target INT DEFAULT 5,
  sql_target INT DEFAULT 5,
  aptitude_target INT DEFAULT 5
);

daily_progress (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  date DATE NOT NULL,
  dsa_done INT DEFAULT 0,
  sql_done INT DEFAULT 0,
  aptitude_done INT DEFAULT 0
);

daily_briefing (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  briefing_date DATE NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

notification_tokens (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  fcm_token TEXT NOT NULL UNIQUE,
  device_type VARCHAR(50),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

---

## 6. Complete REST API Endpoint Directory

### Authentication & Health
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` | Public service health & timestamp check |
| `GET` | `/me` | Authenticated user profile decoded from JWT |

### Timetable & Free Slot Allocation
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/timetable` | Create a timetable slot |
| `POST` | `/timetable/bulk` | Bulk create slots for the week |
| `GET` | `/timetable` | List all timetable slots for user |
| `GET` | `/timetable/day/{day}` | Slots for specific weekday (e.g. `MONDAY`) |
| `DELETE`| `/timetable/{id}` | Delete a timetable slot |
| `GET` | `/timetable/free-slots?day={day}` | Calculate available free study blocks |

### Subjects & Topics (Spaced Repetition)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/subjects` | Create a new subject |
| `GET` | `/subjects` | List all user subjects |
| `GET` | `/subjects/summary` | Subject list with question & topic stats |
| `DELETE`| `/subjects/{id}` | Delete subject and all associated topics/questions |
| `DELETE`| `/subjects/all` | Wipe all subjects and topics for clean reset |
| `POST` | `/topics` | Create a topic under a subject |
| `GET` | `/topics/subject/{subjectId}` | List topics under subject |
| `POST` | `/topics/{id}/review` | Submit SM-2 review score (0–5) |
| `GET` | `/topics/due-for-revision` | List topics due for revision today |
| `DELETE`| `/topics/{id}` | Delete a topic |

### Practice & Dynamic 7-Column Excel Import
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/practice/import` | Multipart 7-column Excel (`.xlsx`/`.xls`) or PDF parser |
| `POST` | `/practice/questions` | Create a single practice question |
| `GET` | `/practice/questions` | List questions by category (`DSA`, `SQL`, `APTITUDE`) |
| `PATCH`| `/practice/questions/{id}/status` | Update status (`SOLVED`, `IN_PROGRESS`, etc.) |
| `POST` | `/practice/questions/{id}/review` | Submit SM-2 review rating |
| `DELETE`| `/practice/questions/{id}` | Delete a practice question |
| `DELETE`| `/practice/clear-all` | Wipe all practice questions |
| `GET` | `/practice/quota-config` | Get daily quota targets |
| `PUT` | `/practice/quota-config` | Update daily quota targets |
| `GET` | `/practice/today-quota` | Get today's completed vs target quotas |

### DSA Tracker
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/dsa/import` | Upload Excel to DSA repository |
| `POST` | `/dsa` | Create DSA question |
| `GET` | `/dsa` | List all DSA questions |
| `GET` | `/dsa/today?limit=5` | Get today's DSA question queue |
| `POST` | `/dsa/{id}/review` | Review DSA question (SM-2) |
| `DELETE`| `/dsa/{id}` | Delete DSA question |
| `DELETE`| `/dsa/all` | Wipe all DSA questions |

### Tasks & Priority Events
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/tasks` | Create a task |
| `GET` | `/tasks?status=PENDING` | List tasks filtered by status |
| `PATCH`| `/tasks/{id}/status` | Mark task as `PENDING` or `DONE` |
| `DELETE`| `/tasks/{id}` | Delete a task |
| `POST` | `/priority-events` | Create exam/deadline priority event |
| `GET` | `/priority-events` | List all priority events |
| `GET` | `/priority-events/upcoming` | List upcoming priority events |
| `DELETE`| `/priority-events/{id}` | Delete a priority event |

### Scheduler & Executive Briefings
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/schedule/today` | Generate today's time-blocked schedule |
| `GET` | `/schedule/date?date=YYYY-MM-DD` | Generate schedule for specific date |
| `GET` | `/briefing/today` | Today's LLM executive briefing |
| `POST` | `/briefing/today/regenerate` | Force regenerate briefing |

### AI Chat Assistant & Dashboard Metrics
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/chat/message` | Conversational AI chat with system context & actions |
| `GET` | `/dashboard/metrics` | Real-time KPI aggregation across all modules |

### Notifications (FCM)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/notifications/fcm-token` | Register device FCM push token |
| `GET` | `/notifications/pending` | Fetch unread notifications |
| `GET` | `/notifications` | List all notifications |
| `POST` | `/notifications/{id}/read` | Mark notification as read |

---

## 7. Algorithms & Execution Logic

### A. Deterministic Time-Blocking Scheduler
1. Fetches all `FREE` timetable slots for target day from `TimetableService`.
2. Inspects `PriorityEventService` for upcoming exams/tests within $\le 7$ days (boosts slot allocation).
3. Queries `TopicService` and `PracticeService` for items where `next_revision_at <= today`.
4. Allocates daily practice quota (`DSA`, `SQL`, `Aptitude`) into remaining free windows.
5. Emits an ordered list of `ScheduledSlotItem` records with start/end times and activity labels.

### B. Modified SuperMemo SM-2 Spaced Repetition
$$\text{Ease Factor}' = \max\left(1.3, \text{Ease Factor} + (0.1 - (5 - q) \times (0.08 + (5 - q) \times 0.02))\right)$$
$$\text{Interval}' = \begin{cases} 1 & \text{if repetition count} = 0 \\ 6 & \text{if repetition count} = 1 \\ \text{Interval} \times \text{Ease Factor} & \text{if } q \ge 3 \\ 1 & \text{if } q < 3 \end{cases}$$

### C. Conversational AI Chat Agent
1. User sends a message via `POST /chat/message`.
2. `ChatAgentService` injects live context from all services:
   - Today's schedule & free slots
   - Due revision topics & exam dates
   - Daily quota progress (DSA, SQL, Aptitude)
   - Pending tasks
   - Subject summary and question statistics
3. Dispatches prompt to Groq (`llama-3.1-8b-instant`).
4. If `LLM_API_KEY` is not set or network fails, automatically falls back to deterministic template classification.

---

## 8. Deployment & Running Locally

### Prerequisites:
- Java 21+ / Java 24
- Maven 3.9+
- Python 3 (for static web server) or Docker

### Running Locally:
```bash
# 1. Run Spring Boot Backend (Port 8080)
cd backend && mvn spring-boot:run

# 2. Run Frontend Web Dashboard (Port 3000)
python3 -m http.server 3000 --directory frontend

# 3. Or run both via Docker Compose
docker-compose up --build
```

### Automated Test Suite:
```bash
cd backend && mvn test
# Results: 50/50 unit tests passing (BUILD SUCCESS)
```

