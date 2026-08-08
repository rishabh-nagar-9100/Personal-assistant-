-- V6: Create dsa_questions table
CREATE TABLE dsa_questions (
    id                UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title             VARCHAR(255)     NOT NULL,
    topic             VARCHAR(100)     NOT NULL,
    difficulty        VARCHAR(10)      NOT NULL DEFAULT 'MEDIUM',
    source_link       TEXT,
    status            VARCHAR(20)      NOT NULL DEFAULT 'NOT_STARTED',
    last_attempted_at TIMESTAMPTZ,
    next_revision_at  TIMESTAMPTZ,
    ease_factor       DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval_days     INT              NOT NULL DEFAULT 1,
    repetition_count  INT              NOT NULL DEFAULT 0
);

CREATE INDEX idx_dsa_user_topic ON dsa_questions(user_id, topic);
CREATE INDEX idx_dsa_next_revision ON dsa_questions(next_revision_at);
