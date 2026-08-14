CREATE INDEX ix_normalised_events_project_timeline_order
    ON normalised_ci_events (organisation_id, project_id, occurred_at, received_at, id);

CREATE INDEX ix_pipeline_runs_project_timeline_updated
    ON pipeline_runs (organisation_id, project_id, updated_at DESC, id DESC);