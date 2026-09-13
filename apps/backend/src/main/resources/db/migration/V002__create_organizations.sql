CREATE TYPE industry_type AS ENUM (
    'RETAIL', 'AGENCY', 'FREELANCER', 'RESTAURANT', 'SERVICE', 'OTHER'
);

CREATE TYPE member_role AS ENUM (
    'OWNER', 'ADMIN', 'ACCOUNTANT', 'STAFF', 'AUDITOR'
);

CREATE TABLE organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    industry industry_type NOT NULL DEFAULT 'OTHER',
    tax_number VARCHAR(100),
    country_code CHAR(2) NOT NULL DEFAULT 'US',
    currency_code CHAR(3) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE organization_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role member_role NOT NULL DEFAULT 'STAFF',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(organization_id, user_id)
);

CREATE INDEX idx_org_members_user ON organization_members(user_id);
CREATE INDEX idx_org_members_org ON organization_members(organization_id);
