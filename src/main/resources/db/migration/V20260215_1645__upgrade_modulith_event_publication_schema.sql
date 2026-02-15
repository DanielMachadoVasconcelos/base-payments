-- Keep lookup deterministic across environments.
SET search_path TO orders, public;

ALTER TABLE IF EXISTS orders.event_publication
    ADD COLUMN IF NOT EXISTS completion_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_resubmission_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'PROCESSING';

-- Existing rows from the old schema get a status derived from completion_date.
UPDATE orders.event_publication
SET status = CASE
    WHEN completion_date IS NULL THEN 'PROCESSING'
    ELSE 'COMPLETED'
END
WHERE status IS NULL OR status = 'PROCESSING';
