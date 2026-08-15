CREATE TABLE recommendation_reviews (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    recommendation_id UUID NOT NULL,
    reviewed_version_id UUID,
    action VARCHAR(16) NOT NULL,
    reason_category VARCHAR(32) NOT NULL,
    comment VARCHAR(500),
    reviewer_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_review_project_tenant FOREIGN KEY (project_id, organisation_id) REFERENCES projects (id, organisation_id),
    CONSTRAINT ck_review_action CHECK (action IN ('ACCEPT', 'EDIT', 'REJECT')),
    CONSTRAINT ck_review_reason CHECK (reason_category IN ('NONE', 'NOT_GROUNDED', 'INCORRECT_SCOPE', 'DUPLICATE', 'UNSAFE', 'OTHER')),
    CONSTRAINT ck_review_comment CHECK (comment IS NULL OR length(trim(comment)) > 0),
    CONSTRAINT ck_review_rejection_reason CHECK (action <> 'REJECT' OR reason_category <> 'NONE')
);

CREATE TABLE reviewed_recommendation_versions (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    recommendation_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    summary VARCHAR(2000) NOT NULL,
    likely_cause VARCHAR(1000),
    reviewer_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_reviewed_version_project_tenant FOREIGN KEY (project_id, organisation_id) REFERENCES projects (id, organisation_id),
    CONSTRAINT ck_reviewed_version_number CHECK (version_number > 0),
    CONSTRAINT ck_reviewed_version_summary CHECK (length(trim(summary)) > 0),
    CONSTRAINT uk_reviewed_version_number UNIQUE (organisation_id, project_id, recommendation_id, version_number)
);

CREATE TABLE incident_resolutions (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    incident_id UUID NOT NULL,
    recommendation_id UUID NOT NULL,
    reviewed_version_id UUID NOT NULL,
    resolution_text VARCHAR(2000) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_resolution_project_tenant FOREIGN KEY (project_id, organisation_id) REFERENCES projects (id, organisation_id),
    CONSTRAINT fk_resolution_incident_tenant FOREIGN KEY (incident_id, project_id, organisation_id) REFERENCES incidents (id, project_id, organisation_id),
    CONSTRAINT ck_resolution_text CHECK (length(trim(resolution_text)) > 0)
);

CREATE INDEX ix_reviews_recommendation_time ON recommendation_reviews (organisation_id, project_id, recommendation_id, created_at DESC, id DESC);
CREATE INDEX ix_reviewed_versions_recommendation ON reviewed_recommendation_versions (organisation_id, project_id, recommendation_id, version_number DESC);
CREATE INDEX ix_resolutions_incident_time ON incident_resolutions (organisation_id, project_id, incident_id, created_at DESC, id DESC);
