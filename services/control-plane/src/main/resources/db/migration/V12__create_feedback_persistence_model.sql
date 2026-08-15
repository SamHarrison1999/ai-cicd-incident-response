CREATE TABLE feedback_signals (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    recommendation_id UUID NOT NULL,
    review_id UUID NOT NULL,
    outcome VARCHAR(16) NOT NULL,
    policy_version VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_feedback_signal_project_tenant FOREIGN KEY (project_id, organisation_id) REFERENCES projects (id, organisation_id),
    CONSTRAINT ck_feedback_signal_outcome CHECK (outcome IN ('ACCEPTED', 'EDITED', 'REJECTED', 'RESOLVED'))
);

CREATE TABLE feedback_aggregates (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    policy_version VARCHAR(64) NOT NULL,
    window_start TIMESTAMPTZ NOT NULL,
    window_end TIMESTAMPTZ NOT NULL,
    sample_count INTEGER NOT NULL,
    accepted_count INTEGER NOT NULL,
    edited_count INTEGER NOT NULL,
    rejected_count INTEGER NOT NULL,
    resolved_count INTEGER NOT NULL,
    suppression_reason VARCHAR(32) NOT NULL,
    CONSTRAINT fk_feedback_aggregate_project_tenant FOREIGN KEY (project_id, organisation_id) REFERENCES projects (id, organisation_id),
    CONSTRAINT ck_feedback_aggregate_window CHECK (window_end >= window_start),
    CONSTRAINT ck_feedback_aggregate_counts CHECK (sample_count >= 0 AND accepted_count >= 0 AND edited_count >= 0 AND rejected_count >= 0 AND resolved_count >= 0),
    CONSTRAINT ck_feedback_aggregate_suppression CHECK (suppression_reason IN ('NONE', 'INSUFFICIENT_SAMPLE', 'AMBIGUOUS_SAMPLE'))
);

CREATE INDEX ix_feedback_signals_scope_time ON feedback_signals (organisation_id, project_id, created_at ASC, id ASC);
CREATE INDEX ix_feedback_aggregates_scope_window ON feedback_aggregates (organisation_id, project_id, window_end DESC, id DESC);
