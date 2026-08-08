-- V4: Create tasks and priority_events tables
CREATE TABLE tasks (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    due_date        TIMESTAMPTZ  NOT NULL,
    priority        VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    linked_topic_id UUID,
    status          VARCHAR(10)  NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_tasks_user_due ON tasks(user_id, due_date);

CREATE TABLE priority_events (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name              VARCHAR(255) NOT NULL,
    event_date        TIMESTAMPTZ  NOT NULL,
    type              VARCHAR(30)  NOT NULL,
    jd_text           TEXT,
    boosted_topic_ids TEXT[]
);

CREATE INDEX idx_priority_events_user_date ON priority_events(user_id, event_date);
