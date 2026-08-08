-- V11: Add day_order support to timetable_slots and user_daily_state table
ALTER TABLE timetable_slots ADD COLUMN IF NOT EXISTS day_order VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_timetable_slots_user_day_order ON timetable_slots(user_id, day_order);

CREATE TABLE IF NOT EXISTS user_daily_state (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    day_order VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_user_daily_state_user_date UNIQUE (user_id, date)
);

CREATE INDEX IF NOT EXISTS idx_user_daily_state_user_date ON user_daily_state(user_id, date);
