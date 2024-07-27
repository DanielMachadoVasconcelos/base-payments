CREATE TABLE IF NOT EXISTS orders (
    id serial PRIMARY KEY NOT NULL
);

CREATE TABLE IF NOT EXISTS order_line_items (
    id serial PRIMARY KEY NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
     orders  INT references orders (id)
);
