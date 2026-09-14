CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS financial_report_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    report_type VARCHAR(50) NOT NULL, -- PROFIT_AND_LOSS, BALANCE_SHEET, CASH_FLOW, TRIAL_BALANCE, AR_AGING
    period_id UUID,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'USD',
    report_data JSONB NOT NULL,
    generated_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_report_snapshots_org_type ON financial_report_snapshots(organization_id, report_type, from_date, to_date);

CREATE TABLE IF NOT EXISTS financial_kpi_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    as_of_date DATE NOT NULL,
    gross_profit_margin NUMERIC(7, 4),
    net_profit_margin NUMERIC(7, 4),
    current_ratio NUMERIC(9, 4),
    quick_ratio NUMERIC(9, 4),
    total_revenue NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    total_expenses NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    net_income NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    cash_balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    ar_outstanding NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    ap_outstanding NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_kpi_snapshots_org_date ON financial_kpi_snapshots(organization_id, as_of_date);
