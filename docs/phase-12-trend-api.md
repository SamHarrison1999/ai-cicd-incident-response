# Phase 12 trend API

Batch 3 exposes the persisted operational-learning projections as bounded read-only responses.

## Routes

- `GET /api/v1/organisations/{organisationId}/projects/{projectId}/operational-learning/trends`
- `GET /api/v1/organisations/{organisationId}/projects/{projectId}/operational-learning/trends/compare`

Both routes require an authenticated active organisation member. Filters are validated before a maximum page size of 50 is applied. Ordering remains stable by window end, dimension, dimension key, and identifier.

The comparison response describes adjacent persisted observations and a bounded delta. It is advisory and does not infer a root cause or trigger a provider, policy, incident, or production change.
