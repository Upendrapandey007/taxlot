CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE expense_status AS ENUM ('RECORDED', 'VOID');
CREATE TYPE expense_category AS ENUM (
    'MEALS', 'TRAVEL', 'OFFICE_SUPPLIES', 'UTILITIES', 'RENT', 'ADVERTISING', 'SOFTWARE', 'OTHER'
);
CREATE TYPE expense_payment_method AS ENUM (
    'CASH', 'BANK_TRANSFER', 'CREDIT_CARD', 'OTHER'
);

CREATE TABLE expenses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    expense_number  VARCHAR(50) NOT NULL,
    vendor_name     VARCHAR(255) NOT NULL,
    category        expense_category NOT NULL,
    expense_date    DATE NOT NULL,
    payment_method  expense_payment_method NOT NULL DEFAULT 'BANK_TRANSFER',
    currency_code   CHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate   NUMERIC(19,6) NOT NULL DEFAULT 1.000000,
    subtotal        NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    tax_amount      NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    total_amount    NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    receipt_url     VARCHAR(500),
    notes           TEXT,
    status          expense_status NOT NULL DEFAULT 'RECORDED',
    created_by      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, expense_number)
);
CREATE INDEX idx_expenses_org_date ON expenses(organization_id, expense_date);
CREATE INDEX idx_expenses_org_category ON expenses(organization_id, category);

CREATE TABLE expense_outbox (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id       UUID NOT NULL UNIQUE,
    event_type     VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   UUID NOT NULL,
    tenant_id      UUID,
    routing_key    VARCHAR(200) NOT NULL,
    payload        TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMPTZ,
    attempt_count  INT NOT NULL DEFAULT 0,
    last_error     TEXT
);
CREATE INDEX idx_expense_outbox_unpub ON expense_outbox(created_at) WHERE published_at IS NULL;
