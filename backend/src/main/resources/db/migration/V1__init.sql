-- V1: Dummy migration to prove Flyway is working against Supabase Postgres
CREATE TABLE IF NOT EXISTS flyway_proof (
    id   SERIAL PRIMARY KEY,
    note TEXT DEFAULT 'Flyway migrations are working'
);

INSERT INTO flyway_proof (note) VALUES ('Initial bootstrap migration applied successfully');
