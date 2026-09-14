CREATE TABLE IF NOT EXISTS regulatory_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    jurisdiction_id UUID NOT NULL REFERENCES tax_jurisdictions(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    source_url TEXT NOT NULL,
    scraper_type VARCHAR(50) NOT NULL, -- IRD_PORTAL, GAZETTE, CIRCULAR_FEED, GENERIC_HTML
    last_scraped_at TIMESTAMPTZ,
    last_content_hash VARCHAR(64),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS regulatory_scrapes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL REFERENCES regulatory_sources(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL, -- SUCCESS, FAILED, UNCHANGED
    content_hash VARCHAR(64),
    scraped_content_summary TEXT,
    changes_detected BOOLEAN NOT NULL DEFAULT FALSE,
    error_message TEXT,
    scraped_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tax_rule_revisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id UUID NOT NULL REFERENCES tax_rules(id) ON DELETE CASCADE,
    scrape_id UUID REFERENCES regulatory_scrapes(id) ON DELETE SET NULL,
    change_type VARCHAR(50) NOT NULL, -- RATE_AMENDMENT, THRESHOLD_UPDATE, NEW_EXEMPTION, STATUTORY_UPDATE
    old_rate NUMERIC(7, 4),
    new_rate NUMERIC(7, 4),
    old_threshold NUMERIC(19, 4),
    new_threshold NUMERIC(19, 4),
    rationale TEXT NOT NULL,
    effective_from DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regulatory_scrapes_source ON regulatory_scrapes(source_id, scraped_at DESC);
CREATE INDEX idx_tax_rule_revisions_rule ON tax_rule_revisions(rule_id, created_at DESC);
