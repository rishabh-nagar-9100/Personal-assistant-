-- V5: Create subjects and topics tables
CREATE TABLE subjects (
    id      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name    VARCHAR(255) NOT NULL
);

CREATE INDEX idx_subjects_user ON subjects(user_id);

CREATE TABLE topics (
    id               UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    subject_id       UUID             NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    name             VARCHAR(255)     NOT NULL,
    status           VARCHAR(20)      NOT NULL DEFAULT 'NOT_STARTED',
    last_studied_at  TIMESTAMPTZ,
    next_revision_at TIMESTAMPTZ,
    ease_factor      DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    interval_days    INT              NOT NULL DEFAULT 1,
    repetition_count INT              NOT NULL DEFAULT 0
);

CREATE INDEX idx_topics_next_revision ON topics(next_revision_at);
