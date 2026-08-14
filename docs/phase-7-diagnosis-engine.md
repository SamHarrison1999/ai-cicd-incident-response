# Phase 7 deterministic diagnosis engine

Batch 3 implements the diagnosis contract over the sanitised evidence boundary.

`DeterministicDiagnosisEngine` evaluates bounded signal text against explicit
rules for dependency failure, deployment regression, configuration change, and
resource exhaustion. It returns a suspected category only when the evidence
supports one unambiguous rule. Empty, unmatched, and tied inputs abstain safely.

The result includes the rule version, bounded confidence, supporting signal
identifiers, missing-evidence notices, warnings, and an abstention reason. It
does not expose evidence content or make a causal claim.
