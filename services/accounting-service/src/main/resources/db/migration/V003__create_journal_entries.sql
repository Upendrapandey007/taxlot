CREATE TYPE entry_source_type AS ENUM (
    'MANUAL', 'INVOICE', 'PAYMENT', 'EXPENSE', 'OPENING_BALANCE', 'REVERSAL'
);

CREATE TYPE entry_status AS ENUM (
    'POSTED', 'REVERSED'
);

CREATE TABLE journal_entries (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id      UUID NOT NULL,
    period_id            UUID NOT NULL REFERENCES accounting_periods(id),
    entry_number         VARCHAR(50) NOT NULL,
    entry_date           DATE NOT NULL,
    reference            VARCHAR(255),
    description          TEXT,
    source_type          entry_source_type NOT NULL DEFAULT 'MANUAL',
    source_id            UUID,
    status               entry_status NOT NULL DEFAULT 'POSTED',
    reversed_by_entry_id UUID,
    created_by           UUID,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    posted_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, entry_number)
);
CREATE INDEX idx_journal_entries_org_date ON journal_entries(organization_id, entry_date);
CREATE INDEX idx_journal_entries_org_source ON journal_entries(organization_id, source_type, source_id);