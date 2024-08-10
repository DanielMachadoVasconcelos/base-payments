SET search_path TO orders;

DELETE FROM orders WHERE 1 = 1;
DELETE FROM orders_aud WHERE 1 = 1;
DELETE FROM orders_revision WHERE 1 = 1;
DELETE FROM event_publication WHERE 1 = 1;