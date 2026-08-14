# Phase 5 correlation engine

## Policy v1

`incident-correlation-v1` uses a 30-minute window and threshold 5.

| Dimension | Weight | Match rule |
|---|---:|---|
| Commit | 3 | Both values exist and are equal |
| Environment | 2 | Both values exist and are equal |
| Event family | 2 | Both events are pipeline or both are deployment events |
| Pipeline run | 1 | External run ID and attempt are equal |
| Time window | 1 | Event and candidate detection are within 30 minutes |

Organisation and project equality are mandatory gates. A resolved incident is not a candidate for new evidence. Failure, cancellation, and timeout statuses are eligible; successful, queued, running, skipped, and unknown statuses are not eligible in this batch.

## Stable selection

Candidates that meet the threshold are ordered by:

1. highest score;
2. earliest incident detection time;
3. lexicographically smallest incident ID.

The engine returns the selected incident or no selection when no candidate reaches the threshold. It also returns the sorted candidate IDs considered within the requested tenant and project boundary.

## Decision record

Each event has one decision record containing the event ID, optional selected incident ID, policy version, score, threshold, matched dimensions, considered candidate IDs, and creation time. The record is bounded metadata and contains no raw payload, signature, or secret.

The decision recorder is idempotent on event ID and emits an audit action named `INCIDENT_CORRELATION_DECISION`.

## Scope boundary

This engine identifies likely relationships. It does not prove causality, call an AI provider, contact production systems, assess reliability effect, or execute remediation.