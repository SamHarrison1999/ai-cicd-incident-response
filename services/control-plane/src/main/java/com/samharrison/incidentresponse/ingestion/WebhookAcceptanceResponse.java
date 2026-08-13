package com.samharrison.incidentresponse.ingestion;

import java.time.Instant;
import java.util.UUID;

public record WebhookAcceptanceResponse(
    UUID deliveryId, boolean duplicate, WebhookDeliveryStatus status, Instant receivedAt) {}
