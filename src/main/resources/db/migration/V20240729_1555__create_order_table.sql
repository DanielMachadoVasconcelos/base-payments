-- Create the orders schema if it does not exist
CREATE SCHEMA IF NOT EXISTS orders;

-- Set the search path to the orders schema
SET search_path TO orders;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the event_store table
CREATE TABLE IF NOT EXISTS event_store (
    id VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aggregated_identifier VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    version INT NOT NULL CHECK (version >= 0),
    event_type VARCHAR(255) NOT NULL,
    event_data TEXT NOT NULL,
    UNIQUE (aggregated_identifier, version)
);

-- Create an index on the aggregated_identifier column
CREATE INDEX idx_aggregated_identifier ON event_store (aggregated_identifier);