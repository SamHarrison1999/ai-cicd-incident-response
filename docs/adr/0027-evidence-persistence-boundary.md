# ADR 0027: Evidence persistence and redaction boundary

Evidence is redacted before content hashing, persistence, or future indexing.
The stored projection contains bounded technical content, a deterministic
SHA-256 hash, provenance fields, and an explicit retention class. Secret
material and raw signatures are never accepted as viewer-ready evidence.

Retention calculation is deterministic and auditable. This batch does not
delete evidence or expose search endpoints; later batches must preserve the
same tenant and bounded-content rules.