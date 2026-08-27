-- Initial schema: jobs, job_notes, attachments
--
-- status/payment_status use TEXT + CHECK rather than a native Postgres ENUM
-- type: Exposed/JDBC would otherwise need explicit casts on every parameter
-- bind, which adds driver-level complexity for no real benefit at this scale.

CREATE TABLE jobs (
    id                  BIGSERIAL PRIMARY KEY,
    title               TEXT NOT NULL,
    category            TEXT,
    location            TEXT,
    vendor_name         TEXT,
    vendor_contact      TEXT,
    status              TEXT NOT NULL DEFAULT 'quoted'
                            CHECK (status IN ('quoted', 'scheduled', 'in_progress', 'done', 'cancelled')),
    quoted_cost         NUMERIC(12, 2),
    actual_cost         NUMERIC(12, 2),
    predicted_hours     NUMERIC(8, 2),
    actual_hours        NUMERIC(8, 2),
    scheduled_date      DATE,
    completed_date      DATE,
    warranty_expiry     DATE,
    payment_status      TEXT NOT NULL DEFAULT 'unpaid'
                            CHECK (payment_status IN ('unpaid', 'partial', 'paid')),
    payment_method      TEXT,
    -- Stored as TIMESTAMP (no tz): the app treats every instant as UTC end-to-end
    -- (Exposed's Instant-backed `timestamp()` column type expects this), which
    -- sidesteps any JDBC session-timezone conversion surprises.
    created_at          TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at          TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_jobs_status ON jobs (status);
CREATE INDEX idx_jobs_category ON jobs (category);
CREATE INDEX idx_jobs_location ON jobs (location);
CREATE INDEX idx_jobs_scheduled_date ON jobs (scheduled_date);

CREATE TABLE job_notes (
    id          BIGSERIAL PRIMARY KEY,
    job_id      BIGINT NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    "timestamp" TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    body        TEXT NOT NULL
);

CREATE INDEX idx_job_notes_job_id ON job_notes (job_id);

CREATE TABLE attachments (
    id          BIGSERIAL PRIMARY KEY,
    job_id      BIGINT NOT NULL REFERENCES jobs (id) ON DELETE CASCADE,
    file_name   TEXT NOT NULL,
    label       TEXT,
    taken_at    TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc')
);

CREATE INDEX idx_attachments_job_id ON attachments (job_id);
