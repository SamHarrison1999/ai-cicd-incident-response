# Phase 8 end-to-end scenarios

## Authorised historical context

An active organisation member submits a bounded diagnosis, provider, and
environment query. The API returns sanitised historical summaries, match
explanations, source identifiers, and provenance references for the same
project. A second request using `nextCursor` returns the next deterministic
page.

## Empty and ambiguous context

When no authorised record matches, the response contains an empty item list and
does not manufacture a recommendation. When multiple records have equal
metadata support, all relevant bounded matches remain visible and the UI labels
them as historical context rather than a confirmed cause.

## Rejection scenarios

Cross-organisation access, unknown projects, malformed cursors, invalid time
ranges, and limits above the configured maximum are rejected. Responses never
include raw payloads, signatures, credentials, or unsanitised instructions.
