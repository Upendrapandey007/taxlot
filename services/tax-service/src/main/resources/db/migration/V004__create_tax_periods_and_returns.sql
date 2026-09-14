CREATE TABLE IF NOT EXISTS tax_periods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    period_name VARCHAR(50) NOT NULL, -- e.g. "2081/04", "2024-Q1", "2024-07"
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, FILED, LOCKED
    filing_deadline DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tax_period UNIQUE (organization_id, period_name)
);

CREATE TABLE IF NOT EXISTS tax_return_summaries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    period_id UUID NOT NULL REFERENCES tax_periods(id) ON DELETE CASCADE,
    total_taxable_sales NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    total_exempt_sales NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    output_vat_collected NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    total_taxable_purchases NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    total_exempt_purchases NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    input_vat_paid NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    net_vat_payable NUMERIC(19, 4) NOT NULL DEFAULT 0.0000, -- Output VAT - Input VAT (if negative, refundable/credit)
    total_tds_withheld NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- DRAFT, FINALIZED, SUBMITTED
    notes TEXT,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tax_periods_org ON tax_periods(organization_id, start_date);
CREATE INDEX idx_tax_return_org_period ON tax_return_summaries(organization_id, period_id);
