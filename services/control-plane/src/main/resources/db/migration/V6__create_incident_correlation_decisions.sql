CREATE TABLE incident_correlation_decisions (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    event_id UUID NOT NULL,
    incident_id UUID,
    policy_version VARCHAR(80) NOT NULL,
    score INTEGER NOT NULL,
    threshold INTEGER NOT NULL,
    matched_dimensions JSONB NOT NULL,
    considered_candidates JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_correlation_decision_project_tenant
        FOREIGN KEY (project_id, organisation_id)
        REFERENCES projects (id, organisation_id),
    CONSTRAINT fk_correlation_decision_event_tenant
        FOREIGN KEY (event_id, project_id, organisation_id)
        REFERENCES normalised_ci_events (id, project_id, organisation_id),
    CONSTRAINT fk_correlation_decision_incident_tenant
        FOREIGN KEY (incident_id, project_id, organisation_id)
        REFERENCES incidents (id, project_id, organisation_id),
    CONSTRAINT uk_correlation_decision_event UNIQUE (event_id),
    CONSTRAINT ck_correlation_decision_score CHECK (score >= 0),
    CONSTRAINT ck_correlation_decision_threshold CHECK (threshold > 0),
    CONSTRAINT ck_correlation_decision_dimensions
        CHECK (jsonb_typeof(matched_dimensions) = 'array'),
    CONSTRAINT ck_correlation_decision_candidates
        CHECK (jsonb_typeof(considered_candidates) = 'array')
);

CREATE INDEX ix_correlation_decisions_tenant_created
    ON incident_correlation_decisions (organisation_id, project_id, created_at DESC);

CREATE INDEX ix_correlation_decisions_incident_created
    ON incident_correlation_decisions (incident_id, created_at DESC)
    WHERE incident_id IS NOT NULL;