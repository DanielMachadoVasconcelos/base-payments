-- Create the orders schema if it does not exist
CREATE SCHEMA IF NOT EXISTS orders;

-- Set the search path to the orders schema
SET search_path TO orders;

CREATE TABLE IF NOT EXISTS orders
(
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    version      BIGINT       NOT NULL DEFAULT 1,
    payload TEXT,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

ALTER TABLE orders
    ADD COLUMN created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN modified_at TIMESTAMPTZ,
    ADD COLUMN created_by  VARCHAR,
    ADD COLUMN modified_by VARCHAR;

create sequence orders_revision_seq INCREMENT BY 1 START WITH 1;

create table orders_revision
(
    rev      BIGINT NOT NULL PRIMARY KEY DEFAULT nextval('orders_revision_seq'),
    revtstmp BIGINT
);

ALTER sequence orders_revision_seq OWNED BY orders_revision.rev;

CREATE TABLE orders_aud
(
    id   uuid   NOT NULL,
    rev         BIGINT NOT NULL REFERENCES orders_revision (rev),
    revtype     smallint,
    version     bigint,
    payload   text,
    created_at  timestamp(6),
    created_by  varchar(255),
    modified_at timestamp(6),
    modified_by varchar(255),
    PRIMARY KEY (rev, id)
);
