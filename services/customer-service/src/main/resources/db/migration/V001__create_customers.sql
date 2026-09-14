CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE customer_status AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE address_type AS ENUM ('BILLING', 'SHIPPING');

CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    company_name    VARCHAR(255),
    tax_number      VARCHAR(100),
    currency_code   CHAR(3) NOT NULL DEFAULT 'USD',
    status          customer_status NOT NULL DEFAULT 'ACTIVE',
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_customers_org ON customers(organization_id, status);
CREATE INDEX idx_customers_name ON customers(organization_id, name);

CREATE TABLE customer_addresses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    type            address_type NOT NULL DEFAULT 'BILLING',
    line1           VARCHAR(255) NOT NULL,
    line2           VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(100),
    postal_code     VARCHAR(20),
    country_code    CHAR(2) NOT NULL DEFAULT 'US',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_cust_addresses_cust ON customer_addresses(customer_id);

CREATE TABLE customer_contacts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_cust_contacts_cust ON customer_contacts(customer_id);
