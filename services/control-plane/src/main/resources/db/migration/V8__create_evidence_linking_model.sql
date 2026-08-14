ALTER TABLE evidence_items
    ADD CONSTRAINT uk_evidence_id_project_organisation
    UNIQUE (id, project_id, organisation_id);

CREATE TABLE incident_evidence_links (
    id UUID PRIMARY KEY,
    incident_id UUID NOT NULL,
    evidence_id UUID NOT NULL,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    linked_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_incident_evidence_incident_tenant
        FOREIGN KEY (incident_id, project_id, organisation_id)
        REFERENCES incidents (id, project_id, organisation_id),
    CONSTRAINT fk_incident_evidence_evidence_tenant
        FOREIGN KEY (evidence_id, project_id, organisation_id)
        REFERENCES evidence_items (id, project_id, organisation_id),
    CONSTRAINT uk_incident_evidence_link_pair
        UNIQUE (incident_id, evidence_id),
    CONSTRAINT uk_incident_evidence_link_id_tenant
        UNIQUE (id, incident_id, project_id, organisation_id)
);

CREATE INDEX ix_incident_evidence_links_incident_time
    ON incident_evidence_links (organisation_id, project_id, incident_id, linked_at, id);

CREATE TABLE evidence_event_links (
    id UUID PRIMARY KEY,
    evidence_id UUID NOT NULL,
    event_id UUID NOT NULL,
    organisation_id UUID NOT NULL,
    project_id UUID NOT NULL,
    linked_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_evidence_event_evidence_tenant
        FOREIGN KEY (evidence_id, project_id, organisation_id)
        REFERENCES evidence_items (id, project_id, organisation_id),
    CONSTRAINT fk_evidence_event_event_tenant
        FOREIGN KEY (event_id, project_id, organisation_id)
        REFERENCES normalised_ci_events (id, project_id, organisation_id),
    CONSTRAINT uk_evidence_event_link_pair
        UNIQUE (evidence_id, event_id),
    CONSTRAINT uk_evidence_event_link_id_tenant
        UNIQUE (id, evidence_id, project_id, organisation_id)
);

CREATE INDEX ix_evidence_event_links_event_time
    ON evidence_event_links (organisation_id, project_id, event_id, linked_at, id);
