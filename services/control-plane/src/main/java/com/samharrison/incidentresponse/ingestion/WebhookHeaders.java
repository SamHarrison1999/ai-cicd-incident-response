package com.samharrison.incidentresponse.ingestion;

public final class WebhookHeaders {

  public static final String DELIVERY_ID = "X-CICD-Delivery-ID";
  public static final String EVENT_TYPE = "X-CICD-Event-Type";
  public static final String DELIVERY_TIMESTAMP = "X-CICD-Delivery-Timestamp";
  public static final String SIGNATURE = "X-CICD-Signature";

  private WebhookHeaders() {}
}
