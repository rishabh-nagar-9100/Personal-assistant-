-- V8: Create daily_briefings table
CREATE TABLE daily_briefings (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date          DATE        NOT NULL,
    briefing_text TEXT        NOT NULL,
    generated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_daily_briefings_user_date UNIQUE (user_id, date)
);
