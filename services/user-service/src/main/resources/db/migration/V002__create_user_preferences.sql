CREATE TABLE user_preferences (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               UUID NOT NULL UNIQUE REFERENCES user_profiles(user_id),
    email_notifications   BOOLEAN NOT NULL DEFAULT TRUE,
    push_notifications    BOOLEAN NOT NULL DEFAULT FALSE,
    currency_display      VARCHAR(3) NOT NULL DEFAULT 'USD',
    date_format           VARCHAR(20) NOT NULL DEFAULT 'YYYY-MM-DD',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
