ALTER TABLE projects
    ADD CONSTRAINT uk_projects_id_organisation UNIQUE (id, organisation_id);

CREATE TABLE event_sources (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    provider VARCHAR(32) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    status VARCHAR(32) NOT NULL,
    signing_secret_reference VARCHAR(255) NOT NULL,
    signature_algorithm VARCHAR(32) NOT NULL,
    timestamp_tolerance_seconds INTEGER NOT NULL,
    max_payload_size_bytes INTEGER NOT NULL,
    secret_rotated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_event_source_project_tenant
        FOREIGN KEY (project_id, organisation_id)
        REFERENCES projects (id, organisation_id),
    CONSTRAINT uk_event_source_project_display_name
        UNIQUE (project_id, display_name),
    CONSTRAINT uk_event_source_id_project_organisation
        UNIQUE (id, project_id, organisation_id),
    CONSTRAINT ck_event_source_provider
        CHECK (provider IN ('GITHUB_ACTIONS', 'JENKINS')),
    CONSTRAINT ck_event_source_status
        CHECK (status IN ('ENABLED', 'DISABLED')),
    CONSTRAINT ck_event_source_signature_algorithm
        CHECK (signature_algorithm = 'HMAC_SHA256'),
    CONSTRAINT ck_event_source_timestamp_tolerance
        CHECK (timestamp_tolerance_seconds BETWEEN 1 AND 3600),
    CONSTRAINT ck_event_source_payload_size
        CHECK (max_payload_size_bytes BETWEEN 1 AND 1048576),
    CONSTRAINT ck_event_source_secret_reference
        CHECK (length(trim(signing_secret_reference)) > 0)
);

CREATE INDEX ix_event_sources_organisation_project
    ON event_sources (organisation_id, project_id);

CREATE TABLE webhook_deliveries (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    event_source_id UUID NOT NULL,
    provider_delivery_id VARCHAR(200) NOT NULL,
    provider_event_type VARCHAR(100) NOT NULL,
    payload_sha256 VARCHAR(64) NOT NULL,
    delivery_timestamp TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    outcome_code VARCHAR(80),
    processed_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_webhook_delivery_source_tenant
        FOREIGN KEY (event_source_id, project_id, organisation_id)
        REFERENCES event_sources (id, project_id, organisation_id),
    CONSTRAINT uk_webhook_delivery_source_provider_id
        UNIQUE (event_source_id, provider_delivery_id),
    CONSTRAINT uk_webhook_delivery_id_source_tenant
        UNIQUE (id, event_source_id, project_id, organisation_id),
    CONSTRAINT ck_webhook_delivery_status
        CHECK (status IN ('RECEIVED', 'PROCESSED', 'REJECTED', 'FAILED', 'PROCESSING_RETRY')),
    CONSTRAINT ck_webhook_delivery_payload_hash
        CHECK (payload_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_webhook_delivery_processing_outcome
        CHECK (
            (status IN ('RECEIVED', 'PROCESSING_RETRY') AND processed_at IS NULL)
            OR (status IN ('PROCESSED', 'REJECTED', 'FAILED')
                AND processed_at IS NOT NULL
                AND outcome_code IS NOT NULL)
        )
);

CREATE INDEX ix_webhook_deliveries_tenant_received
    ON webhook_deliveries (organisation_id, project_id, received_at DESC);

CREATE INDEX ix_webhook_deliveries_status_received
    ON webhook_deliveries (status, received_at);

CREATE TABLE pipeline_runs (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    event_source_id UUID NOT NULL,
    provider VARCHAR(32) NOT NULL,
    external_run_id VARCHAR(200) NOT NULL,
    name VARCHAR(200) NOT NULL,
    attempt INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    commit_sha VARCHAR(64),
    git_ref VARCHAR(500),
    environment_name VARCHAR(120),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    last_event_occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_pipeline_run_source_tenant
        FOREIGN KEY (event_source_id, project_id, organisation_id)
        REFERENCES event_sources (id, project_id, organisation_id),
    CONSTRAINT uk_pipeline_run_source_external_attempt
        UNIQUE (event_source_id, external_run_id, attempt),
    CONSTRAINT uk_pipeline_run_id_source_tenant
        UNIQUE (id, event_source_id, project_id, organisation_id),
    CONSTRAINT ck_pipeline_run_provider
        CHECK (provider IN ('GITHUB_ACTIONS', 'JENKINS')),
    CONSTRAINT ck_pipeline_run_attempt CHECK (attempt > 0),
    CONSTRAINT ck_pipeline_run_status
        CHECK (status IN (
            'QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED',
            'SKIPPED', 'TIMED_OUT', 'UNKNOWN'
        )),
    CONSTRAINT ck_pipeline_run_completed_at
        CHECK (
            (status IN ('SUCCEEDED', 'FAILED', 'CANCELLED', 'SKIPPED', 'TIMED_OUT')
                AND completed_at IS NOT NULL)
            OR (status IN ('QUEUED', 'RUNNING', 'UNKNOWN') AND completed_at IS NULL)
        )
);

CREATE INDEX ix_pipeline_runs_tenant_updated
    ON pipeline_runs (organisation_id, project_id, updated_at DESC);

CREATE INDEX ix_pipeline_runs_status_updated
    ON pipeline_runs (status, updated_at DESC);

CREATE TABLE normalised_ci_events (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    event_source_id UUID NOT NULL,
    webhook_delivery_id UUID NOT NULL,
    pipeline_run_id UUID,
    schema_version VARCHAR(16) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    external_run_id VARCHAR(200) NOT NULL,
    pipeline_name VARCHAR(200) NOT NULL,
    run_attempt INTEGER NOT NULL,
    pipeline_status VARCHAR(32) NOT NULL,
    commit_sha VARCHAR(64),
    git_ref VARCHAR(500),
    environment_name VARCHAR(120),
    evidence_summary VARCHAR(1000) NOT NULL,
    source_fields JSONB NOT NULL,
    CONSTRAINT fk_normalised_event_source_tenant
        FOREIGN KEY (event_source_id, project_id, organisation_id)
        REFERENCES event_sources (id, project_id, organisation_id),
    CONSTRAINT fk_normalised_event_delivery_tenant
        FOREIGN KEY (webhook_delivery_id, event_source_id, project_id, organisation_id)
        REFERENCES webhook_deliveries (id, event_source_id, project_id, organisation_id),
    CONSTRAINT fk_normalised_event_pipeline_run_tenant
        FOREIGN KEY (pipeline_run_id, event_source_id, project_id, organisation_id)
        REFERENCES pipeline_runs (id, event_source_id, project_id, organisation_id),
    CONSTRAINT uk_normalised_event_webhook_delivery UNIQUE (webhook_delivery_id),
    CONSTRAINT ck_normalised_event_provider
        CHECK (provider IN ('GITHUB_ACTIONS', 'JENKINS')),
    CONSTRAINT ck_normalised_event_type
        CHECK (event_type IN (
            'PIPELINE_RUN_STARTED', 'PIPELINE_RUN_COMPLETED',
            'PIPELINE_JOB_STARTED', 'PIPELINE_JOB_COMPLETED',
            'DEPLOYMENT_STARTED', 'DEPLOYMENT_COMPLETED'
        )),
    CONSTRAINT ck_normalised_event_pipeline_status
        CHECK (pipeline_status IN (
            'QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED',
            'SKIPPED', 'TIMED_OUT', 'UNKNOWN'
        )),
    CONSTRAINT ck_normalised_event_run_attempt CHECK (run_attempt > 0),
    CONSTRAINT ck_normalised_event_source_fields
        CHECK (jsonb_typeof(source_fields) = 'array')
);

CREATE INDEX ix_normalised_events_tenant_occurred
    ON normalised_ci_events (organisation_id, project_id, occurred_at);

CREATE INDEX ix_normalised_events_pipeline_run_occurred
    ON normalised_ci_events (pipeline_run_id, occurred_at)
    WHERE pipeline_run_id IS NOT NULL;
