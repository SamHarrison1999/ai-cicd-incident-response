# Phase 12 end-to-end scenarios

## Authorised tenant trend viewing

An authenticated active member selects an organisation and project, filters a trend dimension, and receives only projections belonging to that tenant. A user without active membership receives the standard inaccessible-resource response.

## Deterministic comparison

Two adjacent persisted windows are returned in stable order with a bounded count delta. Repeating the same query produces the same ordering and values.

## Suppressed sample

A small, stale, ambiguous, or conflicting sample is represented with suppression metadata and is not presented as an actionable provider or remediation signal.

## Workspace safety

The learning workspace renders aggregate metadata, provenance, observation windows, and suppression state. It does not render raw evidence, review comments, secrets, provider prompts, or production controls.
