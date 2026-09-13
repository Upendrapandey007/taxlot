CREATE TABLE organization_settings (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id       UUID NOT NULL UNIQUE REFERENCES organizations(id) ON DELETE CASCADE,
    fiscal_year_start     INT NOT NULL DEFAULT 1,
    invoice_prefix        VARCHAR(20) NOT NULL DEFAULT 'INV',
    invoice_next_number   INT NOT NULL DEFAULT 1,
    expense_approval      BOOLEAN NOT NULL DEFAULT FALSE,
    timezone              VARCHAR(50) NOT NULL DEFAULT 'UTC',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
