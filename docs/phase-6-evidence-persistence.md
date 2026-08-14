# Phase 6 evidence persistence

Batch 2 adds the tenant-owned evidence item, V7 migration, typed evidence and
retention classifications, redaction before hashing, deterministic content
hashes, retention-boundary calculation, and service-level tenant checks.

The service stores only the redacted projection. Search, incident linking,
retention deletion workflows, API responses, and viewer components remain
later Phase 6 work.