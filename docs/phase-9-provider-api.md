# Phase 9 Batch 3: provider adapters and recommendation API

## Provider-neutral boundary

`RecommendationProvider` receives only `ProviderRecommendationRequest`, which contains bounded summaries, hashes, and provenance identifiers. `ProviderRecommendationCandidate` contains a bounded category, explanation, confidence, and abstention reason.

## Deterministic fallback

`DeterministicRecommendationProvider` applies stable keyword rules to sanitised evidence. It abstains for empty, ambiguous, or instruction-bearing bundles. Provider errors do not create remediation actions.

## API

`POST /api/v1/organisations/{organisationId}/projects/{projectId}/recommendations` generates one bounded recommendation. `GET` lists project-scoped recommendations. The endpoint requires active tenant membership and does not expose raw provider prompts or content outside the bounded response.
