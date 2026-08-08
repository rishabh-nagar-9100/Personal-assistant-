-- V7: Create practice_questions, daily_quota_config, and daily_progress tables
CREATE TABLE practice_questions (
    id                UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_type     VARCHAR(20)      NOT NULL,
    sub_category      VARCHAR(100),
    title             VARCHAR(255)     NOT NULL,
    difficulty        VARCHAR(10)      NOT NULL DEFAULT 'MEDIUM',
    status            VARCHAR(20)      NOT NULL DEFAULT 'NOT_STARTED',
    last_attempted_at TIMESTAMPTZ,
    next_revision_at  TIMESTAMPTZ,
    ease_factor       DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval_days     INT              NOT NULL DEFAULT 1,
    repetition_count  INT              NOT NULL DEFAULT 0
);

CREATE INDEX idx_practice_user_cat ON practice_questions(user_id, category_type);

CREATE TABLE daily_quota_config (
    user_id         UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    dsa_target      INT  NOT NULL DEFAULT 5,
    sql_target      INT  NOT NULL DEFAULT 5,
    aptitude_target INT  NOT NULL DEFAULT 5
);

CREATE TABLE daily_progress (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date          DATE NOT NULL,
    dsa_done      INT  NOT NULL DEFAULT 0,
    sql_done      INT  NOT NULL DEFAULT 0,
    aptitude_done INT  NOT NULL DEFAULT 0,
    CONSTRAINT uq_daily_progress_user_date UNIQUE (user_id, date)
);
