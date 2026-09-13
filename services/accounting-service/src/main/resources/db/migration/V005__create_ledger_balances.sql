CREATE TABLE ledger_balances (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    account_id      UUID NOT NULL REFERENCES accounts(id),
    period_id       UUID NOT NULL REFERENCES accounting_periods(id),
    debit_total     NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    credit_total    NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    net_balance     NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, account_id, period_id)
);
CREATE INDEX idx_ledger_balances_lookup ON ledger_balances(organization_id, period_id, account_id);