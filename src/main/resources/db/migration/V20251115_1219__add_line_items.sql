-- Create line_items table
CREATE TABLE IF NOT EXISTS orders.line_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_minor_units BIGINT NOT NULL CHECK (unit_price_minor_units >= 0),
    reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders.orders(order_id) ON DELETE CASCADE
);

-- Index for efficient order lookup
CREATE INDEX IF NOT EXISTS idx_line_items_order_id ON orders.line_items(order_id);

-- Note: orders.amount remains for query efficiency (denormalized)
-- Consistency ensured via application logic
