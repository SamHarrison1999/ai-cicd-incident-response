# Phase 3 final verification checklist

Phase 3 is ready for its final pull request only when:

- all six progress-ledger batches are COMPLETE_VERIFIED;
- local repository, Java, frontend, and Compose checks pass;
- both GitHub Actions-shaped and Jenkins-shaped signed simulations are repeatable;
- duplicate delivery IDs return deterministic duplicate responses;
- changed delivery payloads return a conflict;
- pipeline-run projections are visible through tenant-scoped API reads;
- no raw secret, payload, signature, or generated report is tracked;
- the verification output is recorded in docs/progress/phase-3.md.

The final PR is opened only after this checklist is satisfied. Required CI checks must pass before merging into main.
