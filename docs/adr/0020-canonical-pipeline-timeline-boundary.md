# ADR 0020: Use a versioned canonical pipeline timeline boundary

## Status

Accepted for Phase 4 Batch 1.

## Context

Phase 3 verifies signed deliveries and maps selected provider payloads into provider-neutral pipeline candidates. The product still needs a stable read model for pipeline runs and event timelines. Provider payloads change independently, retries can arrive out of order, and equal timestamps cannot provide a complete ordering guarantee.

## Decision

Phase 4 introduces a versioned canonical timeline contract between provider adapters and the pipeline read model. Each canonical event must carry:

- a stable event identifier;
- organisation and project ownership;
- event-source and provider metadata;
- external delivery and run identity where available;
- event type and lifecycle status;
- occurred-at and received-at timestamps;
- a deterministic ordering key;
- safe, bounded summary fields suitable for the UI.

Raw payloads and supplied signatures remain outside this read model. The canonical contract contains allow-listed fields and evidence references, not arbitrary provider JSON.

Pipeline runs are keyed by project, event source, external run identity, and attempt. Events may arrive out of order, so lifecycle transitions are monotonic and terminal states cannot regress. Timeline reads use a deterministic ordering of occurred-at, received-at, and stable event identifier, with cursor pagination for repeatable traversal.

## Consequences

The contract makes provider adapters replaceable and gives the API and frontend one stable representation. It requires explicit schema-version handling, migration discipline, and tests for duplicate, late, and equal-timestamp events. It does not decide incident membership; that belongs to Phase 5.

## Rejected alternatives

- Exposing provider payloads directly to the frontend: rejected because it leaks provider-specific structure and untrusted fields.
- Ordering only by event timestamp: rejected because equal timestamps and late arrivals make pagination unstable.
- Using a generic JSON document as the canonical model: rejected because it weakens validation, tenant boundaries, and auditability.