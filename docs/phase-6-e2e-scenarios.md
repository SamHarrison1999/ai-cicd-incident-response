# Phase 6 synthetic end-to-end scenarios

These scenarios describe deterministic offline checks for the evidence path.
They are verification scenarios, not production claims or proof of causality.

## Scenario 1: bounded evidence reaches an authorised viewer

1. A tenant-owned technical observation is submitted with provenance and an
   occurrence timestamp.
2. Secret-like values and signatures are redacted before hashing and storage.
3. Metadata search returns the evidence kind, source, timestamps, hash, and
   bounded identifiers without raw content.
4. An authorised viewer requests the selected item inside the same
   organisation/project scope.
5. The response contains only persisted redacted content and bounded incident
   or event links.

Expected result: the viewer can inspect the observation without receiving
secret material, a signature, an unbounded payload, or hidden policy details.

## Scenario 2: duplicate processing is safe

1. The same deterministic evidence input is processed twice.
2. The content hash and tenant ownership remain stable.
3. The second processing attempt does not create an inconsistent link or a
   second projection for the same idempotency boundary.

Expected result: repeated delivery is safe and does not amplify evidence or
incident associations.

## Scenario 3: cross-tenant access is rejected

1. A viewer from organisation A requests evidence owned by organisation B.
2. A search request uses organisation A with a project identifier owned by B.
3. A link request attempts to associate evidence and an incident across tenant
   boundaries.

Expected result: every operation is rejected or returns an inaccessible-resource
result without revealing the other tenant's content or identifiers.

## Scenario 4: bounded content remains bounded

1. An input exceeds the configured content, line, or source-reference bound.
2. A payload contains bearer-token, secret, credential, or signature material.
3. The input is processed through the evidence boundary.

Expected result: oversized input is rejected before persistence and sensitive
material is absent from hashes, search responses, viewer output, and frontend
rendering.
