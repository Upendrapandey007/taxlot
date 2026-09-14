CREATE TABLE IF NOT EXISTS tax_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jurisdiction_id UUID NOT NULL REFERENCES tax_jurisdictions(id) ON DELETE CASCADE,
    category_code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    rate NUMERIC(7, 4) NOT NULL DEFAULT 0.0000, -- e.g. 0.1300 for 13% VAT, 0.0150 for 1.5% TDS
    threshold_amount NUMERIC(19, 4),             -- e.g. 50000.0000 threshold
    is_reverse_charge BOOLEAN NOT NULL DEFAULT FALSE,
    is_exempt BOOLEAN NOT NULL DEFAULT FALSE,
    legal_reference VARCHAR(255) NOT NULL,       -- e.g. "VAT Act 2052, Section 7"
    description TEXT,
    effective_from DATE NOT NULL,
    effective_to DATE,                           -- NULL means indefinitely effective
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tax_rules_lookup ON tax_rules(jurisdiction_id, category_code, is_active, effective_from);
CREATE INDEX idx_tax_rules_effective ON tax_rules(effective_from, effective_to);
