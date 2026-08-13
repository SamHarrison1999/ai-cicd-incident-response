package com.samharrison.incidentresponse.ingestion;

import org.springframework.http.HttpStatus;

public class WebhookIngestionException extends RuntimeException {

  private final HttpStatus status;
  private final String code;

  WebhookIngestionException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  static WebhookIngestionException badRequest(String code, String message) {
    return new WebhookIngestionException(HttpStatus.BAD_REQUEST, code, message);
  }

  static WebhookIngestionException unauthorized(String code, String message) {
    return new WebhookIngestionException(HttpStatus.UNAUTHORIZED, code, message);
  }

  static WebhookIngestionException notFound() {
    return new WebhookIngestionException(
        HttpStatus.NOT_FOUND,
        "EVENT_SOURCE_NOT_FOUND",
        "The requested event source is unavailable.");
  }

  static WebhookIngestionException conflict() {
    return new WebhookIngestionException(
        HttpStatus.CONFLICT,
        "WEBHOOK_DELIVERY_PAYLOAD_CONFLICT",
        "The delivery identifier has already been used with a different event type or payload.");
  }

  static WebhookIngestionException payloadTooLarge() {
    return new WebhookIngestionException(
        HttpStatus.PAYLOAD_TOO_LARGE,
        "WEBHOOK_PAYLOAD_TOO_LARGE",
        "The webhook payload exceeds the configured event-source limit.");
  }

  static WebhookIngestionException unsupportedMediaType() {
    return new WebhookIngestionException(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "WEBHOOK_CONTENT_TYPE_UNSUPPORTED",
        "Webhook requests must use application/json.");
  }

  static WebhookIngestionException secretUnavailable() {
    return new WebhookIngestionException(
        HttpStatus.SERVICE_UNAVAILABLE,
        "WEBHOOK_SECRET_UNAVAILABLE",
        "The event source is temporarily unavailable.");
  }
}
