# Intelligence Service

The intelligence service is the Python and FastAPI component responsible for safe log analysis.

## Planned responsibilities

- Log sanitisation.
- Secret redaction.
- Prompt-injection detection.
- Deterministic failure classification.
- Historical incident retrieval.
- Evidence-grounded recommendation generation.
- Confidence scoring and abstention.
- Evaluation execution.
- AI-provider abstraction.

## Ownership boundary

The service receives bounded, sanitised analysis requests from the Java control plane and returns structured recommendations. It does not mutate authoritative incident workflow state.

The executable FastAPI project is added in Phase 1 Batch 3.
