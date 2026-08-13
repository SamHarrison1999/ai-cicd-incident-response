# Phase 3 repeatable simulations

Set WEBHOOK_SECRET_LOCAL_SIMULATOR to the same local secret value referenced by the configured event source, then run:

.\scripts\simulate-signed-webhook.ps1 -EventSourceId <event-source-uuid> -Provider GITHUB_ACTIONS
.\scripts\simulate-signed-webhook.ps1 -EventSourceId <event-source-uuid> -Provider JENKINS

The simulator binds the delivery ID, event type, exact timestamp and exact JSON bytes using the Phase 3 signature envelope. Reusing a delivery ID with unchanged bytes demonstrates idempotency; changing the payload demonstrates conflict detection.
