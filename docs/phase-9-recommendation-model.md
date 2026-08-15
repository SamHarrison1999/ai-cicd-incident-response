# Phase 9 recommendation model

## Goal

Add provider-neutral, evidence-grounded recommendations for human review while
preserving tenant isolation, sanitisation, provenance, bounded confidence, and
explicit abstention.

Phase 9 consumes the Phase 6 evidence, Phase 7 diagnosis, and Phase 8
historical-retrieval boundaries. It does not introduce autonomous remediation,
production-changing actions, or unbounded model access.

## Recommendation contract

A recommendation request contains:

- one organisation and project scope;
- an optional incident and pipeline context;
- bounded sanitised evidence references;
- bounded historical retrieval projections;
- the diagnosis hypotheses available to the reviewer; and
- a versioned recommendation policy.

A recommendation projection contains:

- a bounded category and summary;
- proposed next investigative steps, not executable commands;
- evidence and historical-record references;
- provider and model or ruleset provenance;
- confidence represented as a bounded value with an explanation; and
- an explicit status such as `RECOMMENDED`, `ABSTAINED`, or `REJECTED`.

## Safety rules

- Requests are authorised within organisation and project scope before assembly.
- Only sanitised, bounded content may enter the provider request.
- Provider output is untrusted and must pass schema and length validation.
- Prompt-injection-like evidence is data, not instruction.
- Conflicting or insufficient evidence produces abstention.
- A recommendation cannot mutate incidents, pipelines, deployments, or hosts.
- Every result keeps enough provenance for a reviewer to reproduce the input
  boundary and understand why the result was produced.

## Batch plan

| Batch | Scope |
|---|---|
| 1 | Provider abstraction, recommendation contract, and safety boundary |
| 2 | Recommendation persistence and evidence-bundle assembly |
| 3 | Provider adapters, deterministic fallback, and recommendation API |
| 4 | Recommendation workspace and bounded presentation |
| 5 | Security, end-to-end, documentation, and Phase 9 verification |

## Batch 1 acceptance criteria

- The provider boundary is replaceable and versioned.
- Recommendation inputs and outputs are bounded and tenant-scoped.
- Evidence references and provider provenance are mandatory.
- Confidence, abstention, and unsafe-output handling are explicit.
- The project status and Phase 9 progress ledger identify the new phase.