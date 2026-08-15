# ADR 0053: feedback workspace boundary

## Decision

Add a read-only feedback workspace for authorised operators. The page supports organisation and project scope, policy and date filters, aggregate selection, and suppression visibility. It presents governance analytics without exposing raw review text or controls that train providers, modify policy, or execute remediation.

## Consequences

- Operators can inspect bounded feedback signals in the same authenticated workspace as recommendations and review history.
- Empty, filtered, and suppressed results remain explicit states.
- The browser never receives raw evidence, credentials, provider prompts, or hidden tenant data.
