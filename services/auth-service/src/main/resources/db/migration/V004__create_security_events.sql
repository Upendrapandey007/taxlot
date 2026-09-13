CREATE TABLE security_events (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credential_id UUID REFERENCES credentials(id) ON DELETE SET NULL,
    event_type    VARCHAR(50) NOT NULL,
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(500),
    metadata      JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_security_events_credential ON security_events(credential_id);
CREATE INDEX idx_security_events_type       ON security_events(event_type);
