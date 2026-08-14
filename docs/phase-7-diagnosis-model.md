# Phase 7 sanitisation and deterministic diagnosis model

## Goal

Transform authorised Phase 6 evidence into bounded, sanitised technical signals
and deterministic diagnosis results suitable for human review.

Phase 7 is decision support. It does not establish causality, replace an
engineer's judgement, call a production model provider, or execute a recovery
action.

## Sanitisation contract

Sanitisation accepts only evidence already authorised by organisation and
project scope. It produces a bounded projection containing:

- evidence identifiers and provenance references;
- the sanitiser version and processing timestamp;
- typed signal categories and normalised values;
- redacted content within explicit size and line limits; and
- warnings for omitted, malformed, or unsafe input.

Technical text is untrusted data. It must not alter system instructions,
diagnosis rules, authorization decisions, or remediation permissions. Common
prompt-injection patterns, embedded credentials, bearer tokens, signatures,
and secret-like values are removed or represented by safe redaction markers.

## Deterministic diagnosis contract

A diagnosis result contains:

- a versioned rule-set identifier;
- a bounded diagnosis category or `UNKNOWN`;
- a confidence value within the documented range;
- supporting sanitised signal identifiers;
- missing-evidence and safety warnings; and
- an explicit abstention reason when classification is not justified.

Equal inputs, rule versions, and configuration produce the same result. The
result describes supported observations; it does not claim that one observation
caused an incident.

## Initial diagnosis outcomes

- `DEPENDENCY_FAILURE_SUSPECTED`
- `DEPLOYMENT_REGRESSION_SUSPECTED`
- `CONFIGURATION_CHANGE_SUSPECTED`
- `RESOURCE_EXHAUSTION_SUSPECTED`
- `INSUFFICIENT_EVIDENCE`
- `UNKNOWN`

The `SUSPECTED` suffix is intentional: these are bounded hypotheses for human
review, not confirmed root causes.

## Phase boundaries

Batch 1 defines the contract. Later batches may implement sanitisation,
deterministic rules, APIs, and frontend presentation. Provider adapters,
historical retrieval, model-backed recommendations, human review workflows,
and remediation remain separate phase outcomes.
