CREATE TYPE period_status AS ENUM (
    'OPEN', 'CLOSING', 'LOCKED'
);

CREATE TABLE accounting_periods (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    name            VARCHAR(100) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    status          period_status NOT NULL DEFAULT 'OPEN',
    locked_at       TIMESTAMPTZ,
    locked_by       UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, start_date, end_date)
);
CREATE INDEX idx_accounting_periods_org ON accounting_periods(organization_id, status);