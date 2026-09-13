CREATE TABLE journal_lines (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
    organization_id  UUID NOT NULL,
    account_id       UUID NOT NULL REFERENCES accounts(id),
    line_number      INT NOT NULL,
    debit            NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    credit           NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
    currency_code    CHAR(3) NOT NULL DEFAULT 'USD',
    exchange_rate    NUMERIC(19,6) NOT NULL DEFAULT 1.000000,
    description      VARCHAR(500),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_positive_amounts CHECK (debit >= 0 AND credit >= 0),
    CONSTRAINT chk_debit_or_credit CHECK (
        (debit > 0 AND credit = 0) OR (credit > 0 AND debit = 0)
    )
);
CREATE INDEX idx_journal_lines_entry ON journal_lines(journal_entry_id);
CREATE INDEX idx_journal_lines_org_account ON journal_lines(organization_id, account_id);