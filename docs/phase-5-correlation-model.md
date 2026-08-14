# Phase 5 incident correlation model

## Goal

Turn related normalised failure evidence into a tenant-scoped incident projection and a guarded lifecycle. The model is deterministic, policy-versioned, and suitable for synthetic technical verification.

## Input boundary

Correlation consumes only the bounded Phase 4 normalised event representation:

| Field | Correlation use |
|---|---|
| `organisationId` and `projectId` | Required tenant and project boundary |
| `eventId` | Stable evidence identity and decision input |
| `eventType` and pipeline status | Failure eligibility and event-family dimension |
| `occurredAt` and `receivedAt` | Window membership and deterministic ordering |
| `externalRunId` and `attempt` | Pipeline-run identity dimension |
| `commitSha` | Exact commit dimension when present on both events |
| `environmentName` | Exact environment dimension when present on both events |
| `evidenceSummary` and `sourceFields` | Bounded descriptive evidence metadata |

Raw payloads, signatures, signing secrets, and provider-specific unbounded fields are outside this boundary.

## Correlation policy v1

The policy identifier is `incident-correlation-v1`. Its configuration contains a bounded time window, the failure event families eligible for correlation, dimension weights, and the minimum score.

An event is eligible only when it represents a failure or health regression and belongs to a known organisation and project. Candidate incidents must belong to the same organisation and project and must be open to new evidence under the lifecycle rules.

The policy evaluates these dimensions:

| Dimension | Rule | Version 1 weight |
|---|---|---:|
| Commit | Both values are present and equal | 3 |
| Environment | Both values are present and equal | 2 |
| Event family | Events are compatible failure or health categories | 2 |
| Pipeline run | External run identity and attempt are equal | 1 |
| Time window | Event occurred within the configured window | 1 |

Organisation and project are mandatory gates, not optional points. A candidate must meet the minimum configured score. Missing optional values do not match and do not create a partial-match score.

If multiple candidates meet the threshold, select the highest score, then the earliest incident creation time, then the lexicographically smallest incident ID. If no candidate meets the threshold, create one new `DETECTED` incident.

Every decision records the policy identifier, policy configuration version, candidate incident IDs considered, matched dimensions, score, threshold, and the resulting incident ID.

## Membership and idempotency

An event may be linked to zero or one primary incident. Reprocessing the same event ID returns the existing association and does not create another incident or another decision effect. A later event may enrich an existing incident, but it must not rewrite the original event identity or correlation decision.

## Incident state machine

| Current state | Allowed next state | Meaning |
|---|---|---|
| `DETECTED` | `TRIAGED`, `RESOLVED` | New correlation is awaiting investigation or was externally/incorrectly resolved |
| `TRIAGED` | `MITIGATING`, `RESOLVED` | Investigation has begun and may move to mitigation or close without mitigation |
| `MITIGATING` | `MONITORING` | A mitigation has started and recovery is being observed |
| `MONITORING` | `RESOLVED`, `MITIGATING` | Recovery is verified or a regression returns the incident to mitigation |
| `RESOLVED` | `REOPENED` | New evidence indicates recurrence or incomplete recovery |
| `REOPENED` | `TRIAGED` | Investigation resumes without creating a replacement incident |

All transitions record the current state, next state, actor or system reason, timestamp, and correlation ID. Invalid transitions are rejected without changing the incident. Incident records are not physically deleted through normal product workflows.

## Safety and evidence boundary

The model describes related evidence. It does not prove root cause, make a reliability-effect claim, recruit or assess human participants, contact production systems, call an AI provider, or execute rollback, restart, deployment, credential, or infrastructure actions.