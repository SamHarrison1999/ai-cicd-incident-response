# Phase 7 evidence sanitisation

Batch 2 implements the sanitisation boundary defined in ADR 0031.

`EvidenceSanitiser` composes the Phase 6 secret redactor and content bounds
with a deterministic prompt-injection-like instruction detector. Suspicious
lines are replaced with `[UNTRUSTED_INSTRUCTION_REMOVED]`. The result carries
the sanitiser version, bounded line count, and warning codes.

`EvidenceService` uses the sanitised representation before calculating the
content hash and saving evidence. This means search, viewer, and later diagnosis
consumers cannot accidentally bypass the sanitisation step by reading a raw
input representation.

The detector is deliberately conservative and deterministic. It is a boundary
control, not a claim that the source text represents a malicious actor.
