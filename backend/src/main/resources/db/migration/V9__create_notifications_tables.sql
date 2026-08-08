-- V9: Create notification_tokens and notifications tables
CREATE TABLE notification_tokens (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcm_token    TEXT        NOT NULL,
    device_type  VARCHAR(20) DEFAULT 'FLUTTER',
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_fcm_token UNIQUE (user_id, fcm_token)
);

CREATE TABLE notifications (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type          VARCHAR(30) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    body          TEXT        NOT NULL,
    is_read       BOOLEAN     NOT NULL DEFAULT FALSE,
    scheduled_for TIMESTAMPTZ NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_scheduled ON notifications(user_id, scheduled_for, is_read);
