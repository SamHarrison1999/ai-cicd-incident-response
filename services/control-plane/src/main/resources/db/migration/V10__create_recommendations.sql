CREATE TABLE recommendations (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    incident_id UUID,
    category VARCHAR(80) NOT NULL,
    summary VARCHAR(2000) NOT NULL,
    likely_cause VARCHAR(1000),
    confidence NUMERIC(4,3) NOT NULL,
    confidence_explanation VARCHAR(500) NOT NULL,
    status VARCHAR(24) NOT NULL,
    abstention_reason VARCHAR(500),
    provider_name VARCHAR(80) NOT NULL,
    model_version VARCHAR(80) NOT NULL,
    prompt_template_version VARCHAR(80) NOT NULL,
    ruleset_version VARCHAR(80) NOT NULL,
    retrieval_set_version VARCHAR(80) NOT NULL,
    schema_version VARCHAR(32) NOT NULL,
    generated_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_recommendation_project_tenant FOREIGN KEY (project_id, organisation_id) REFERENCES projects (id, organisation_id),
    CONSTRAINT fk_recommendation_incident_tenant FOREIGN KEY (incident_id, project_id, organisation_id) REFERENCES incidents (id, project_id, organisation_id),
    CONSTRAINT ck_recommendation_confidence CHECK (confidence >= 0 AND confidence <= 1),
    CONSTRAINT ck_recommendation_status CHECK (status IN ('RECOMMENDED', 'ABSTAINED', 'REJECTED')),
    CONSTRAINT ck_recommendation_summary CHECK (length(trim(summary)) > 0),
    CONSTRAINT ck_recommendation_abstention CHECK (status <> 'ABSTAINED' OR length(trim(abstention_reason)) > 0)
);

CREATE TABLE recommendation_citations (
    id UUID PRIMARY KEY,
    recommendation_id UUID NOT NULL REFERENCES recommendations (id),
    evidence_id UUID,
    historical_record_id UUID,
    claim VARCHAR(500) NOT NULL,
    CONSTRAINT ck_recommendation_citation_source CHECK ((evidence_id IS NOT NULL) <> (historical_record_id IS NOT NULL)),
    CONSTRAINT ck_recommendation_citation_claim CHECK (length(trim(claim)) > 0)
);

CREATE INDEX ix_recommendations_tenant_time ON recommendations (organisation_id, project_id, generated_at DESC, id DESC);
CREATE INDEX ix_recommendation_citations_recommendation ON recommendation_citations (recommendation_id, id);
