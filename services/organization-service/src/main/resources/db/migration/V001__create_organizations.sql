CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE industry_type AS ENUM (
    'RETAIL','AGENCY','FREELANCER','RESTAURANT','SERVICE','OTHER'
);

CREATE TABLE organizations (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255) NOT NULL,
    industry       industry_type NOT NULL DEFAULT 'OTHER',
    tax_number     VARCHAR(100),
    country_code   CHAR(2) NOT NULL DEFAULT 'US',
    currency_code  CHAR(3) NOT NULL DEFAULT 'USD',
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
