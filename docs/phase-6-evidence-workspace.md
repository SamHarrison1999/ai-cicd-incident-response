# Phase 6 evidence viewer and investigation workspace

Batch 4 adds a tenant-scoped detail projection for one evidence item and a
frontend investigation workspace.

The backend verifies organisation membership, project ownership, and evidence
ownership before returning the persisted redacted content. It also returns
bounded incident and event identifiers associated with the evidence item.

The frontend provides organisation/project scope controls, bounded metadata
filters, deterministic search pagination, evidence selection, and a redacted
content viewer. React renders the content as text inside a bounded `<pre>`
surface; the workspace does not interpret or execute evidence content.
