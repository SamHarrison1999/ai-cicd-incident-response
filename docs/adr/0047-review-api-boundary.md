# ADR 0047: review and resolution API boundary

## Decision

Expose human review and resolution operations through tenant-scoped authenticated endpoints. The API accepts bounded review actions and returns bounded review history. A review may accept, edit, or reject a recommendation; rejection requires a reason and edits create a new immutable version.

Incident resolutions require a reviewed recommendation version from the same organisation and project. The API never exposes raw evidence or provider credentials and never executes remediation.

## Authorization

Every request derives the actor from the authenticated principal and verifies active tenant membership. Path identifiers are treated as scope claims, not as permission grants. Cross-tenant and cross-project references are rejected without disclosure.
