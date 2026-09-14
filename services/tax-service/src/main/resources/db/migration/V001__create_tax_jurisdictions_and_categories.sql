CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS tax_jurisdictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_code VARCHAR(2) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tax_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jurisdiction_id UUID NOT NULL REFERENCES tax_jurisdictions(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    tax_type VARCHAR(30) NOT NULL, -- VAT, WITHHOLDING_TDS, INCOME_TAX, EXCISE
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_jurisdiction_category_code UNIQUE (jurisdiction_id, code)
);

CREATE INDEX idx_tax_categories_jurisdiction ON tax_categories(jurisdiction_id);
