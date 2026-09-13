CREATE TYPE invitation_status AS ENUM ('PENDING','ACCEPTED','EXPIRED','CANCELLED');

CREATE TABLE organization_invitations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    invited_email   VARCHAR(255) NOT NULL,
    invited_by      UUID NOT NULL,
    role            member_role NOT NULL DEFAULT 'STAFF',
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    status          invitation_status NOT NULL DEFAULT 'PENDING',
    expires_at      TIMESTAMPTZ NOT NULL,
    accepted_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_invitations_org ON organization_invitations(organization_id);
CREATE INDEX idx_invitations_email ON organization_invitations(invited_email);
