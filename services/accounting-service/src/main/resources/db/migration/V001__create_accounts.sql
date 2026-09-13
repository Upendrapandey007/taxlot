CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE account_type AS ENUM (
    'ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'
);

CREATE TYPE normal_balance AS ENUM (
    'DEBIT', 'CREDIT'
);

CREATE TABLE accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    code            VARCHAR(50) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    type            account_type NOT NULL,
    normal_balance  normal_balance NOT NULL,
    parent_id       UUID REFERENCES accounts(id),
    is_system       BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, code)
);
CREATE INDEX idx_accounts_org_type ON accounts(organization_id, type);
CREATE INDEX idx_accounts_org_code ON accounts(organization_id, code);