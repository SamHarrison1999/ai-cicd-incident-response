# Phase 8 historical retrieval API

The read-only endpoint is:

`GET /api/v1/organisations/{organisationId}/projects/{projectId}/historical-retrieval`

Supported filters are `diagnosisCategory`, `provider`, `pipeline`,
`environment`, `branch`, `commitSha`, `from`, `to`, and `q`. `limit` is bounded
to 100. `nextCursor` is returned when more results exist and must be passed back
as `cursor` without modification.

Responses contain only bounded historical summaries, source identifiers,
timestamps, match explanations, and provenance references.
