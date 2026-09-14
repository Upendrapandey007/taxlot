CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE invoice_status AS ENUM (
    'DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED'
);

CREATE TABLE invoices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL,
    customer_id         UUID NOT NULL,
    invoice_number      VARCHAR(50) NOT NULL,
    status              invoice_status NOT NULL DEFAULT 'DRAFT',
    issue_date          DATE,
    due_date            DATE,
    currency_code       CHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate       NUMERIC(19,6) NOT NULL DEFAULT 1.000000,
    subtotal            NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    tax_total           NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    discount_total      NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    total_amount        NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    amount_paid         NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    outstanding_balance NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    notes               TEXT,
    terms               TEXT,
    customer_snapshot   JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    issued_at           TIMESTAMPTZ,
    UNIQUE(organization_id, invoice_number)
);
CREATE INDEX idx_invoices_org_status ON invoices(organization_id, status);
CREATE INDEX idx_invoices_customer ON invoices(organization_id, customer_id);

CREATE TABLE invoice_lines (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id      UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    line_number     INT NOT NULL,
    description     VARCHAR(500) NOT NULL,
    quantity        NUMERIC(19,4) NOT NULL DEFAULT 1.0000,
    unit_price      NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    tax_rate        NUMERIC(7,4) NOT NULL DEFAULT 0.0000,
    tax_amount      NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    subtotal        NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    total_amount    NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_invoice_lines_invoice ON invoice_lines(invoice_id);

CREATE TABLE invoice_outbox (
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
CREATE INDEX idx_invoice_outbox_unpub ON invoice_outbox(created_at) WHERE published_at IS NULL;
