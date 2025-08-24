-- Create the orders schema if it does not exist
CREATE SCHEMA IF NOT EXISTS orders;

-- Set the search path to the orders schema
SET search_path TO orders;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create the event_store table
CREATE TABLE IF NOT EXISTS order_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aggregated_identifier VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    version INT NOT NULL CHECK (version >= 0),
    event_type VARCHAR(255) NOT NULL,
    event_data TEXT NOT NULL,
    UNIQUE (aggregated_identifier, version)
);

-- Create an index on the aggregated_identifier column
CREATE INDEX IF NOT EXISTS idx_aggregated_identifier ON order_events (aggregated_identifier);

-- Create the order_status enum if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
        CREATE TYPE order_status AS ENUM ('CREATED', 'CONFIRMED', 'CANCELLED');
    END IF;
END$$;

-- Create the order table
CREATE TABLE IF NOT EXISTS orders (
    order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version INT NOT NULL CHECK (version >= 0) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    order_status  order_status NOT NULL DEFAULT 'CREATED',
    currency VARCHAR(3) NOT NULL,
    amount INTEGER NOT NULL,
    UNIQUE (order_id, version)
);

-- Create an index on the order_status column
CREATE INDEX IF NOT EXISTS idx_order_status ON orders (order_status);