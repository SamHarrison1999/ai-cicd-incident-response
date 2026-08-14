# ADR 0029: Evidence viewer and investigation workspace

## Status

Accepted for Phase 6 Batch 4.

## Decision

The evidence viewer exposes one tenant-scoped, already-redacted evidence
projection at a time. The web workspace uses the metadata search endpoint for
selection and loads the bounded detail projection only for the selected item.

## Constraints

- The viewer requires active organisation membership and a project-owned
  evidence identifier.
- Raw webhook payloads, signatures, credentials, and secret material are not
  reconstructed or returned.
- Viewer content is bounded by the Batch 2 redaction limits.
- Incident and event links are returned as identifiers only.
- The workspace is an evidence inspection surface, not an AI diagnosis or
  remediation surface.
