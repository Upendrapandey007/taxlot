CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE payment_status AS ENUM ('COMPLETED', 'REFUNDED');
CREATE TYPE payment_method_type AS ENUM (
    'CASH', 'BANK_TRANSFER', 'CREDIT_CARD', 'CHEQUE', 'OTHER'
);

CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    payment_number  VARCHAR(50) NOT NULL,
    customer_id     UUID NOT NULL,
    amount          NUMERIC(19,4) NOT NULL,
    currency_code   CHAR(3) NOT NULL DEFAULT 'USD',
    payment_date    DATE NOT NULL,
    payment_method  payment_method_type NOT NULL DEFAULT 'BANK_TRANSFER',
    reference       VARCHAR(100),
    notes           TEXT,
    status          payment_status NOT NULL DEFAULT 'COMPLETED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, payment_number)
);
CREATE INDEX idx_payments_org_date ON payments(organization_id, payment_date);
CREATE INDEX idx_payments_customer ON payments(organization_id, customer_id);

CREATE TABLE payment_allocations (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id       UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    organization_id  UUID NOT NULL,
    invoice_id       UUID NOT NULL,
    allocated_amount NUMERIC(19,4) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_payment_alloc_payment ON payment_allocations(payment_id);
CREATE INDEX idx_payment_alloc_invoice ON payment_allocations(organization_id, invoice_id);

CREATE TABLE payment_outbox (
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
CREATE INDEX idx_payment_outbox_unpub ON payment_outbox(created_at) WHERE published_at IS NULL;
