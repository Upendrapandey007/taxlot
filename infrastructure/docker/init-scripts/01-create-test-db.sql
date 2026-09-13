-- Create test database for Testcontainers integration tests
-- This runs automatically when the postgres container first starts

CREATE DATABASE taxlot_test;
GRANT ALL PRIVILEGES ON DATABASE taxlot_test TO taxlot;
