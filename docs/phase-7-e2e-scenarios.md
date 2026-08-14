# Phase 7 end-to-end scenarios

The Phase 7 close-out scenarios are synthetic and deterministic:

1. Sanitised dependency timeout evidence produces a dependency-failure
   hypothesis with a bounded confidence value.
2. Instruction-like evidence is replaced at the sanitisation boundary and
   cannot change diagnosis rules.
3. Empty or ambiguous evidence produces an explicit abstention.
4. A member can read diagnosis only within an organisation and project scope.
5. Diagnosis output contains identifiers and warnings but not raw evidence,
   credentials, signatures, or remediation commands.

These scenarios support engineering review; they are not proof of causality.
