# Phase 3 observability

The control plane exposes standard Spring Boot actuator and Micrometer endpoints. The ingestion path records:

| Metric | Meaning |
|---|---|
| cicd.ingestion.normalised.events | Normalised CI/CD events created from verified webhook deliveries |
| cicd.ingestion.unsupported.events | Verified webhook deliveries with no supported provider mapping |

Metrics are operational signals, not evidence of root cause. They must be interpreted alongside delivery status, normalised events, and later incident timelines. No request payload, signature, secret, or authentication token is included in metric labels.
