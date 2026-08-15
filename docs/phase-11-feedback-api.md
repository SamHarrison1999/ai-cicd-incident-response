# Phase 11 Batch 3: feedback API

The feedback API provides authenticated, tenant-scoped access to bounded aggregate outcomes. Results are ordered newest window first and capped at fifty items. Optional policy-version and inclusive time-window filters are applied before the cap.

Responses contain aggregate counts, window boundaries, policy version, and suppression reason. They do not contain raw comments, evidence, secrets, prompts, or actions that alter providers or production systems.
