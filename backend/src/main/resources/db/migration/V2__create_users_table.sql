-- V2: Create users table (synced from Supabase Auth)
CREATE TABLE users (
    id         UUID         PRIMARY KEY,   -- matches Supabase auth.users.id
    email      VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
