-- Add column status to orders table
ALTER TABLE orders.orders ADD COLUMN status VARCHAR(128) NOT NULL DEFAULT 'PLACED';