# ADR 0043: recommendation workspace boundary

Status: Accepted

The recommendation workspace presents bounded provider output for human review. It shows confidence, abstention, provenance, and citation identifiers while excluding raw evidence and any execute or remediate control.

The workspace is tenant scoped and explicitly labels recommendations as decision support. A recommendation is not a confirmed cause and cannot change incident state from the browser.
