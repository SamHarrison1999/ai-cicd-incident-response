# Product Scope

## Version 1 vision

Version 1 is a multi-tenant, full-stack incident-response assistant for simulated CI/CD and infrastructure failures. It demonstrates platform engineering, secure event ingestion, incident correlation, observability, evidence-based AI assistance, evaluation, and human governance.

## Primary users

### Platform engineer

Investigates cross-service failures, validates evidence, reviews likely causes, and records resolutions.

### Release engineer

Tracks failing pipeline runs and deployments, compares incidents, and coordinates recovery.

### Software developer

Understands which commit, job, test, dependency, or environment caused a pipeline failure.

### Engineering manager

Reviews incident volume, mean time to acknowledge, recommendation acceptance, classification accuracy, and recurring failure categories.

## Version 1 capabilities

1. Register and authenticate users.
2. Create organisations and projects with tenant-scoped roles.
3. Configure simulated event sources and rotate signing secrets.
4. Receive versioned, signed webhook events through an idempotent API.
5. Persist raw incoming events separately from validated normalised events.
6. Aggregate events into pipeline runs.
7. Correlate related failure events into incidents using deterministic rules.
8. Track incidents through an explicit state machine.
9. Store, search, sanitise, and cite log fragments.
10. Classify incidents using explainable deterministic rules.
11. Retrieve similar resolved incidents.
12. Generate structured recommendations through a provider abstraction.
13. Require evidence citations for generated factual claims.
14. Express confidence and abstain when evidence is insufficient.
15. Allow a human to accept, edit, or reject a recommendation.
16. Record final resolutions and immutable audit events.
17. Run fixed evaluation cases and expose quality metrics.
18. Generate synthetic failure scenarios for demonstrations.
19. Emit structured logs, metrics, and traces.
20. Provide Docker-based local deployment and a documented release process.

## Explicit non-goals

- Automatic rollback, deployment, credential rotation, resource deletion, or other destructive remediation.
- Direct production integrations with GitHub, GitLab, Jenkins, Kubernetes, AWS, Azure, or Google Cloud in version 1.
- General-purpose chatbot functionality.
- Training or fine-tuning a foundation model.
- A fully managed log lake or replacement for commercial observability platforms.
- Arbitrary user-authored code execution.
- Cross-organisation learning from private incident data.
- Claims of causal certainty when only correlation is available.
- High-availability multi-region production operation.
- Billing, subscriptions, enterprise SSO, SCIM, or complex entitlement management.

## Success criteria

The release is portfolio-ready when a reviewer can run one command to start the platform, execute a synthetic failure scenario, inspect the correlated incident and evidence, review an evidence-grounded recommendation, provide feedback, close the incident, and view the result in evaluation and metrics screens.

Quality gates:

- All service-level and end-to-end test suites execute in CI.
- Tenant-isolation tests cover every tenant-owned aggregate exposed by the API.
- Duplicate webhook delivery does not create duplicate domain effects.
- Invalid signatures and unsupported schema versions are rejected and audited.
- Secrets are removed before data reaches any AI provider.
- Every non-abstained generated claim has one or more valid evidence citations.
- The deterministic offline mode supports the complete demonstration.
- No remediation endpoint can execute production-changing actions.

## Representative demonstration scenario

A synthetic deployment pipeline receives a failing integration-test event, a database connection timeout log, and a deployment-health failure for the same project, commit, environment, and correlation window. The platform creates one incident, cites the relevant events and redacted log lines, retrieves a previously resolved connection-pool incident, proposes diagnostic checks with medium confidence, and waits for a release engineer to accept, edit, or reject the recommendation.
