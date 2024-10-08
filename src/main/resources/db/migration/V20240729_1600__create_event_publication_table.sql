-- Set the search path to the orders schema
SET search_path TO orders;

-- Create the event_publication table
CREATE TABLE IF NOT EXISTS orders.event_publication (
    id UUID PRIMARY KEY NOT NULL,
    listener_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx
    ON orders.event_publication (completion_date);

CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx
    ON orders.event_publication USING hash (serialized_event);
