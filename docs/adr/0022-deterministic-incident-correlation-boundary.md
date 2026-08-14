# ADR 0022: Keep incident correlation deterministic and policy-versioned

## Status

Accepted for Phase 5 Batch 1.

## Context

Phase 4 provides tenant-scoped, provider-neutral CI/CD events and deterministic pipeline timelines. The next domain decision is how related failure evidence becomes an incident without making an unsupported causal claim or depending on an external model provider.

Correlation must be repeatable for the same event history, explainable to an engineer, safe across organisation boundaries, and evolvable when the policy changes. The control plane also needs an explicit lifecycle rather than ad hoc status strings.

## Decision

1. Correlation runs in the Java control plane over bounded normalised event fields.
2. Organisation and project identity are mandatory boundaries. Candidate incidents from another organisation or project are never eligible.
3. A versioned deterministic policy scores explicit dimensions: commit, environment, compatible event family, pipeline run identity, and correlation-window membership.
4. Candidate incidents are selected by score, then stable creation time, then stable incident identity. The decision records the policy version, score, dimensions, and candidate set used.
5. Version 1 gives each normalised event zero or one primary incident association. A later relationship model may add secondary links without changing this rule.
6. Incident lifecycle changes use the documented state machine and are recorded as auditable transitions. No transition executes remediation.
7. Raw webhook payloads, signatures, and signing material are not correlation inputs or API response fields.

## Consequences

- The same ordered event history produces the same incident grouping.
- Correlation decisions can be tested with synthetic fixtures and reviewed without an AI provider.
- Policy changes require a new policy version and explicit regression coverage.
- Correlation identifies a likely relationship, not proven causality.
- The domain model can later support evidence, diagnosis, review, and resolution without allowing those concerns into the first correlation boundary.

## Alternatives considered

- Free-form similarity was rejected because it is difficult to audit and reproduce.
- Direct correlation on raw provider payloads was rejected because provider fields are unstable and may contain secrets.
- Automatic remediation was rejected because Phase 5 is decision support only.