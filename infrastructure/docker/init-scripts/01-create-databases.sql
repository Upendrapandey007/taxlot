-- =============================================================================
-- Taxlot — Create one database per microservice
-- Runs automatically when the PostgreSQL container first starts.
-- All databases owned by the same 'taxlot' user for local dev convenience.
-- In production each service uses isolated credentials.
-- =============================================================================

-- taxlot_auth already exists (created as POSTGRES_DB in docker-compose)

CREATE DATABASE taxlot_user;
CREATE DATABASE taxlot_organization;
CREATE DATABASE taxlot_accounting;
CREATE DATABASE taxlot_customer;
CREATE DATABASE taxlot_invoice;
CREATE DATABASE taxlot_payment;
CREATE DATABASE taxlot_expense;
CREATE DATABASE taxlot_tax;
CREATE DATABASE taxlot_reporting;
CREATE DATABASE taxlot_document;
CREATE DATABASE taxlot_notification;
CREATE DATABASE taxlot_ai;
CREATE DATABASE taxlot_search;
CREATE DATABASE taxlot_integration;

GRANT ALL PRIVILEGES ON DATABASE taxlot_user         TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_organization  TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_accounting    TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_customer      TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_invoice       TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_payment       TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_expense       TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_tax           TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_reporting     TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_document      TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_notification  TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_ai            TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_search        TO taxlot;
GRANT ALL PRIVILEGES ON DATABASE taxlot_integration   TO taxlot;

-- Test database for integration tests
CREATE DATABASE taxlot_test;
GRANT ALL PRIVILEGES ON DATABASE taxlot_test TO taxlot;
