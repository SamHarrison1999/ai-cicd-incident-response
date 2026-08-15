CREATE TABLE historical_retrieval_records (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    incident_id UUID,
    source_kind VARCHAR(32) NOT NULL,
    source_id UUID NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    provider VARCHAR(32),
    pipeline_name VARCHAR(200),
    environment_name VARCHAR(120),
    git_ref VARCHAR(500),
    commit_sha VARCHAR(64),
    diagnosis_category VARCHAR(80),
    summary VARCHAR(2000) NOT NULL,
    match_explanation VARCHAR(500) NOT NULL,
    provenance_reference VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_historical_retrieval_project_tenant
        FOREIGN KEY (project_id, organisation_id)
        REFERENCES projects (id, organisation_id),
    CONSTRAINT fk_historical_retrieval_incident_tenant
        FOREIGN KEY (incident_id, project_id, organisation_id)
        REFERENCES incidents (id, project_id, organisation_id),
    CONSTRAINT ck_historical_retrieval_source_kind
        CHECK (source_kind IN ('INCIDENT', 'PIPELINE_RUN', 'EVIDENCE', 'DIAGNOSIS')),
    CONSTRAINT ck_historical_retrieval_summary
        CHECK (length(trim(summary)) > 0),
    CONSTRAINT ck_historical_retrieval_explanation
        CHECK (length(trim(match_explanation)) > 0),
    CONSTRAINT ck_historical_retrieval_provenance
        CHECK (length(trim(provenance_reference)) > 0)
);

CREATE INDEX ix_historical_retrieval_tenant_time
    ON historical_retrieval_records
        (organisation_id, project_id, occurred_at DESC, id DESC);

CREATE INDEX ix_historical_retrieval_tenant_dimensions
    ON historical_retrieval_records
        (organisation_id, project_id, diagnosis_category, provider, environment_name);
