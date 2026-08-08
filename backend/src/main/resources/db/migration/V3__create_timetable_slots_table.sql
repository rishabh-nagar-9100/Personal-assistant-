-- V3: Create timetable_slots table
CREATE TABLE timetable_slots (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10)  NOT NULL,
    start_time  TIME         NOT NULL,
    end_time    TIME         NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    label       VARCHAR(255) NOT NULL,
    CONSTRAINT check_slot_times CHECK (end_time > start_time)
);

CREATE INDEX idx_timetable_slots_user_day ON timetable_slots(user_id, day_of_week);
