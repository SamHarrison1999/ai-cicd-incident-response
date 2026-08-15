# Phase 10 review and resolution model

## Review states

Recommendations enter `PENDING_REVIEW`. An authorised engineer can move them to `ACCEPTED`, `EDITED`, or `REJECTED`. A rejected or superseded generated version remains immutable for audit and evaluation.

## Feedback contract

Feedback records the reviewer, tenant, recommendation version, action, bounded reason category, optional bounded comment, and timestamp. The server derives the actor from authenticated context rather than accepting an arbitrary user identifier.

## Resolution boundary

An accepted or edited recommendation may be proposed as an incident resolution only after the incident and recommendation share the same organisation and project. The final resolution stores reviewed text and citations, not provider prompts, secrets, or raw payloads.

## Safety

Human review is a governance step, not an execution step. No Phase 10 endpoint invokes deployment, rollback, credential rotation, deletion, or other production-changing behaviour.
