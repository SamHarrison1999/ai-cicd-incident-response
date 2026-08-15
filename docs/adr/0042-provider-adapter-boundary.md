# ADR 0042: provider adapter and recommendation API boundary

Status: Accepted

Provider integrations are isolated behind `RecommendationProvider`. The control plane passes a bounded, sanitised evidence bundle and receives a bounded candidate. Providers cannot mutate incidents, execute remediation, access credentials, or return raw evidence.

The first implementation is deterministic and local. It is a safe fallback when no external provider is configured or a provider fails. Insufficient or unsafe evidence produces an abstention with a reason and no action.

The API exposes tenant-scoped recommendation generation and read-only listing. Responses include confidence, citations, abstention state, and immutable provenance.
