ALTER TABLE normalised_ci_events
    ADD CONSTRAINT uk_normalised_event_id_project_organisation
    UNIQUE (id, project_id, organisation_id);

CREATE TABLE incidents (
    id UUID PRIMARY KEY,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_incident_project_tenant
        FOREIGN KEY (project_id, organisation_id)
        REFERENCES projects (id, organisation_id),
    CONSTRAINT uk_incident_id_project_organisation
        UNIQUE (id, project_id, organisation_id),
    CONSTRAINT ck_incident_status
        CHECK (status IN ('DETECTED', 'TRIAGED', 'MITIGATING', 'MONITORING', 'RESOLVED', 'REOPENED')),
    CONSTRAINT ck_incident_title CHECK (length(trim(title)) > 0),
    CONSTRAINT ck_incident_summary CHECK (length(trim(summary)) > 0),
    CONSTRAINT ck_incident_resolved_at
        CHECK ((status = 'RESOLVED' AND resolved_at IS NOT NULL)
            OR (status <> 'RESOLVED' AND resolved_at IS NULL))
);

CREATE INDEX ix_incidents_tenant_detected
    ON incidents (organisation_id, project_id, detected_at DESC, id DESC);

CREATE INDEX ix_incidents_tenant_status
    ON incidents (organisation_id, project_id, status, updated_at DESC);

CREATE TABLE incident_event_links (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    event_id UUID NOT NULL,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    linked_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_incident_event_link_incident_tenant
        FOREIGN KEY (incident_id, project_id, organisation_id)
        REFERENCES incidents (id, project_id, organisation_id),
    CONSTRAINT fk_incident_event_link_event_tenant
        FOREIGN KEY (event_id, project_id, organisation_id)
        REFERENCES normalised_ci_events (id, project_id, organisation_id),
    CONSTRAINT uk_incident_event_link_event UNIQUE (event_id),
    CONSTRAINT uk_incident_event_link_id_tenant
        UNIQUE (id, incident_id, project_id, organisation_id)
);

CREATE INDEX ix_incident_event_links_incident_time
    ON incident_event_links (incident_id, linked_at, event_id);