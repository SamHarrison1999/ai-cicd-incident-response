package com.samharrison.incidentresponse.ingestion;

public enum WebhookDeliveryStatus {
  RECEIVED,
  PROCESSED,
  REJECTED,
  FAILED,
  PROCESSING_RETRY
}
