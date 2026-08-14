CREATE TABLE evidence_items (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    kind VARCHAR(32) NOT NULL,
    retention_class VARCHAR(32) NOT NULL,
    source_system VARCHAR(80) NOT NULL,
    source_reference VARCHAR(200) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    content_line_count INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_evidence_project_tenant FOREIGN KEY (project_id, organisation_id) REFERENCES projects (id, organisation_id),
    CONSTRAINT uk_evidence_tenant_hash UNIQUE (organisation_id, project_id, content_hash),
    CONSTRAINT ck_evidence_content_size CHECK (length(content) <= 12000),
    CONSTRAINT ck_evidence_content_lines CHECK (content_line_count > 0 AND content_line_count <= 201),
    CONSTRAINT ck_evidence_content_hash CHECK (content_hash ~ '^[0-9a-f]{64}$')
);

CREATE INDEX ix_evidence_tenant_occurred ON evidence_items (organisation_id, project_id, occurred_at DESC, id DESC);
CREATE INDEX ix_evidence_tenant_kind_occurred ON evidence_items (organisation_id, project_id, kind, occurred_at DESC);