# ADR 0048: human review workspace boundary

The review workspace is a governance surface, not an automation console. It presents bounded recommendation history and permits authenticated reviewers to accept, edit, reject, or record a resolution. It must not expose raw evidence, provider secrets, or controls that execute remediation.

The page keeps organisation, project, recommendation, and incident identifiers explicit so API requests remain tenant-scoped. Rejection reasons and review comments are visible to reviewers and remain bounded by the API contract.
