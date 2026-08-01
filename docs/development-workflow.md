# Development Workflow

## Branching

`main` is the protected integration branch. Work is completed on short-lived branches and merged through pull requests.

Phase branches use names such as:

```text
chore/phase-1-project-skeleton
feat/phase-2-identity-organisations
feat/phase-3-event-ingestion
```

Smaller batches may be committed independently on the active phase branch.

## Commit messages

Use Conventional Commits:

```text
chore: establish monorepo structure
feat: add signed webhook ingestion
fix: prevent duplicate incoming events
test: add tenant isolation integration tests
docs: document incident correlation rules
```

## Pull requests

A pull request should include:

- A concise summary.
- The relevant phase and batch.
- Significant architecture decisions.
- Commands executed.
- Test output supplied by the developer.
- Documentation changes.
- Known limitations.

Do not state that tests passed unless their output has been observed.

## Review expectations

Review should consider:

- Tenant isolation.
- Idempotency and transaction boundaries.
- Secret handling.
- Evidence grounding.
- Prompt-injection resistance.
- Auditability.
- Operational visibility.
- Backward-compatible API and event schema changes.

## Definition of done

A batch is complete only when:

1. All declared files are implemented.
2. No placeholder-only methods or unresolved TODO markers remain.
3. Relevant tests have been added.
4. The developer has run the documented validation commands.
5. Observed output matches the acceptance criteria.
6. Documentation and the progress ledger are updated.
7. Changes are committed and pushed to the phase branch.
