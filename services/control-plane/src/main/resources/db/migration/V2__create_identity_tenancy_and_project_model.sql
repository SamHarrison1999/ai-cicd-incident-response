CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    normalised_email VARCHAR(320) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_normalised_email UNIQUE (normalised_email),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE TABLE organisations (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_organisations_slug UNIQUE (slug),
    CONSTRAINT ck_organisations_slug CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$')
);

CREATE TABLE organisation_memberships (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_membership_organisation
        FOREIGN KEY (organisation_id) REFERENCES organisations (id),
    CONSTRAINT fk_membership_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_organisation_membership_organisation_user
        UNIQUE (organisation_id, user_id),
    CONSTRAINT ck_membership_role
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    CONSTRAINT ck_membership_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

CREATE INDEX ix_membership_user_status
    ON organisation_memberships (user_id, status);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_project_organisation
        FOREIGN KEY (organisation_id) REFERENCES organisations (id),
    CONSTRAINT uk_project_organisation_slug
        UNIQUE (organisation_id, slug),
    CONSTRAINT ck_project_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT ck_projects_slug CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$')
);

CREATE INDEX ix_projects_organisation
    ON projects (organisation_id);

CREATE TABLE refresh_token_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_family_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    replaced_by_session_id UUID,
    revocation_reason VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL,
    last_used_at TIMESTAMPTZ,
    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_refresh_token_replacement
        FOREIGN KEY (replaced_by_session_id) REFERENCES refresh_token_sessions (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_refresh_token_family
    ON refresh_token_sessions (token_family_id);

CREATE INDEX ix_refresh_token_user
    ON refresh_token_sessions (user_id);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    organisation_id UUID,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id UUID,
    correlation_id VARCHAR(128) NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id),
    CONSTRAINT fk_audit_organisation
        FOREIGN KEY (organisation_id) REFERENCES organisations (id)
);

CREATE INDEX ix_audit_organisation_occurred_at
    ON audit_events (organisation_id, occurred_at DESC);

CREATE INDEX ix_audit_actor_occurred_at
    ON audit_events (actor_user_id, occurred_at DESC);