CREATE TABLE operational_trends (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    dimension VARCHAR(32) NOT NULL,
    dimension_key VARCHAR(96) NOT NULL,
    window_start TIMESTAMPTZ NOT NULL,
    window_end TIMESTAMPTZ NOT NULL,
    aggregation_version VARCHAR(64) NOT NULL,
    sample_count INTEGER NOT NULL,
    observed_count INTEGER NOT NULL,
    source_reference VARCHAR(128) NOT NULL,
    suppression_reason VARCHAR(32) NOT NULL,
    CONSTRAINT fk_operational_trend_project_tenant FOREIGN KEY (project_id, organisation_id) REFERENCES projects (id, organisation_id),
    CONSTRAINT ck_operational_trend_window CHECK (window_end >= window_start),
    CONSTRAINT ck_operational_trend_counts CHECK (sample_count >= 0 AND observed_count >= 0 AND observed_count <= sample_count),
    CONSTRAINT ck_operational_trend_suppression CHECK (suppression_reason IN ('NONE', 'INSUFFICIENT_SAMPLE', 'AMBIGUOUS_SAMPLE', 'STALE_WINDOW'))
);

CREATE INDEX ix_operational_trends_scope_window ON operational_trends (organisation_id, project_id, window_end DESC, dimension ASC, dimension_key ASC, id DESC);
