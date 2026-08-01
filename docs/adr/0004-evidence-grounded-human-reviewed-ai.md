# ADR 0004: Require evidence-grounded, human-reviewed AI output

- Status: Accepted
- Date: 2026-07-30

## Context

CI/CD logs are incomplete, noisy, and attacker-controlled. A model can mistake correlation for causation, invent details, follow malicious instructions embedded in logs, or recommend risky actions. The product must demonstrate useful AI assistance without presenting generated text as authority.

## Decision

Every recommendation uses a versioned structured schema containing:

- summary;
- likely cause;
- confidence level and score;
- supporting evidence citations;
- alternative possible causes;
- suggested next checks;
- abstention flag and reason;
- human review status.

A recommendation may be generated only from evidence identifiers supplied by the control plane. Every factual claim must be associated with citations that Java can resolve to authorized immutable evidence. Low evidence quality causes abstention. Human acceptance, editing, or rejection is required before recommendation text can contribute to a final resolution.

No version 1 component can execute destructive remediation.

## Consequences

### Positive

- Makes safety and trustworthiness visible in the product.
- Enables measurable groundedness and citation quality.
- Preserves human accountability.
- Supports deterministic offline operation.

### Negative

- Reduces fluency and apparent confidence compared with unconstrained chat output.
- Requires claim/citation validation and additional UI workflow.
- Human review adds time to incident handling.

## Guardrails

- Logs are treated as quoted untrusted data.
- Provider adapters cannot expose tools or remediation capabilities.
- Original generated output is immutable after review.
- Acceptance rate is never reported without groundedness and edit/rejection context.
