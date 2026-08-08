-- V10: Add subject and problem number tracking fields to practice_questions and dsa_questions
ALTER TABLE practice_questions
    ADD COLUMN IF NOT EXISTS subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS subject_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS problem_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS source_link TEXT;

ALTER TABLE dsa_questions
    ADD COLUMN IF NOT EXISTS subject_id UUID REFERENCES subjects(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS problem_number VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_practice_subject_id ON practice_questions(subject_id);
CREATE INDEX IF NOT EXISTS idx_practice_subject_name ON practice_questions(user_id, subject_name);
CREATE INDEX IF NOT EXISTS idx_dsa_subject_id ON dsa_questions(subject_id);
