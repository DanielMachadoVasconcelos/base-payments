-- Set the search path to the orders schema
SET search_path TO orders;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;

-- V1__Create_products_table.sql
CREATE TABLE IF NOT EXISTS products (
     product_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT NOT NULL CHECK (version >= 0),
    sku VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    weight BIGINT NOT NULL,
    height BIGINT NOT NULL,
    width BIGINT NOT NULL,
    length BIGINT NOT NULL,
    thumbnail_url VARCHAR(255)
);

ALTER TABLE products
    ALTER COLUMN name TYPE VARCHAR COLLATE "pg_catalog"."C";

CREATE TABLE IF NOT EXISTS product_tags (
    product_id UUID REFERENCES products(product_id),
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_id, tag)
);

CREATE TABLE IF NOT EXISTS product_categories (
    product_id UUID REFERENCES products(product_id),
    category VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_id, category)
);

CREATE TABLE IF NOT EXISTS product_details (
    product_id UUID REFERENCES products(product_id),
    detail TEXT NOT NULL,
    PRIMARY KEY (product_id, detail)
);

CREATE TABLE IF NOT EXISTS product_images_urls (
    product_id UUID REFERENCES products(product_id),
    image_url VARCHAR(1024) NOT NULL,
    PRIMARY KEY (product_id, image_url)
);