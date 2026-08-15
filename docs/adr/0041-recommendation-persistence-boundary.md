# ADR 0041: Recommendation persistence boundary

Status: Accepted

Phase 9 stores bounded recommendation projections and provenance citations. Raw evidence, provider prompts, credentials, and executable remediation instructions are never persisted as recommendation content.

Recommendations are tenant and project scoped. A recommendation may be abstained when the evidence bundle is incomplete, unsafe, or insufficient. Every persisted result records the provider, model, prompt template, ruleset, retrieval set, and schema versions needed to explain how it was produced.

The evidence bundle assembler accepts only existing evidence and historical retrieval projections owned by the requested organisation and project. Citations retain identifiers and short claims, not raw payloads.
