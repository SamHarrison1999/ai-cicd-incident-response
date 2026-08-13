package com.samharrison.incidentresponse.ingestion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class WebhookIngestionService {

  private static final int DELIVERY_ID_MAX_LENGTH = 200;
  private static final int EVENT_TYPE_MAX_LENGTH = 100;
  private static final Pattern SAFE_HEADER_VALUE = Pattern.compile("[A-Za-z0-9._:/-]+");

  private final EventSourceRepository eventSourceRepository;
  private final WebhookSecretResolver secretResolver;
  private final WebhookSignatureService signatureService;
  private final WebhookDeliveryStore deliveryStore;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public WebhookIngestionService(
      EventSourceRepository eventSourceRepository,
      WebhookSecretResolver secretResolver,
      WebhookSignatureService signatureService,
      WebhookDeliveryStore deliveryStore,
      Clock clock) {
    this.eventSourceRepository = eventSourceRepository;
    this.secretResolver = secretResolver;
    this.signatureService = signatureService;
    this.deliveryStore = deliveryStore;
    this.objectMapper = new ObjectMapper();
    this.clock = clock;
  }

  public WebhookAcceptanceResponse ingest(
      UUID eventSourceId,
      String deliveryId,
      String eventType,
      String timestampHeader,
      String signatureHeader,
      String contentTypeHeader,
      long contentLength,
      InputStream requestBody) {
    EventSource eventSource =
        eventSourceRepository
            .findForWebhookIngestionById(eventSourceId)
            .filter(EventSource::isEnabled)
            .orElseThrow(WebhookIngestionException::notFound);

    validateContentType(contentTypeHeader);
    String validatedDeliveryId =
        validateSafeHeader(deliveryId, "delivery ID", DELIVERY_ID_MAX_LENGTH);
    String validatedEventType = validateSafeHeader(eventType, "event type", EVENT_TYPE_MAX_LENGTH);
    String validatedTimestamp = requireHeader(timestampHeader, "delivery timestamp", 80);

    if (!signatureService.hasValidFormat(signatureHeader)) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_SIGNATURE_MALFORMED",
          "The webhook signature must use sha256 followed by lowercase hexadecimal.");
    }

    Instant deliveryTimestamp = parseTimestamp(validatedTimestamp);
    validateTimestampTolerance(deliveryTimestamp, eventSource.getTimestampToleranceSeconds());

    byte[] payload =
        readBoundedPayload(requestBody, contentLength, eventSource.getMaxPayloadSizeBytes());
    byte[] secret =
        secretResolver
            .resolve(eventSource.getSigningSecretReference())
            .orElseThrow(WebhookIngestionException::secretUnavailable);

    try {
      if (!signatureService.verify(
          validatedDeliveryId,
          validatedEventType,
          validatedTimestamp,
          payload,
          secret,
          signatureHeader)) {
        throw WebhookIngestionException.unauthorized(
            "WEBHOOK_SIGNATURE_INVALID", "The webhook signature is invalid.");
      }
    } finally {
      Arrays.fill(secret, (byte) 0);
    }

    validateJson(payload);
    Instant receivedAt = clock.instant();
    WebhookDeliveryStore.StoredWebhookDelivery result =
        deliveryStore.store(
            eventSource,
            validatedDeliveryId,
            validatedEventType,
            signatureService.sha256Hex(payload),
            deliveryTimestamp,
            receivedAt);

    WebhookDelivery delivery = result.delivery();
    return new WebhookAcceptanceResponse(
        delivery.getId(), result.duplicate(), delivery.getStatus(), delivery.getReceivedAt());
  }

  private void validateContentType(String contentTypeHeader) {
    if (contentTypeHeader == null) {
      throw WebhookIngestionException.unsupportedMediaType();
    }
    try {
      MediaType contentType = MediaType.parseMediaType(contentTypeHeader);
      if (!MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
        throw WebhookIngestionException.unsupportedMediaType();
      }
    } catch (InvalidMediaTypeException exception) {
      throw WebhookIngestionException.unsupportedMediaType();
    }
  }

  private String validateSafeHeader(String value, String name, int maximumLength) {
    String candidate = requireHeader(value, name, maximumLength);
    if (!SAFE_HEADER_VALUE.matcher(candidate).matches()) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_HEADER_INVALID", "The webhook " + name + " contains unsupported characters.");
    }
    return candidate;
  }

  private String requireHeader(String value, String name, int maximumLength) {
    if (value == null || value.isBlank()) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_HEADER_MISSING", "The webhook " + name + " header is required.");
    }
    String candidate = value.trim();
    if (candidate.length() > maximumLength) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_HEADER_INVALID", "The webhook " + name + " header is too long.");
    }
    if (candidate.chars().anyMatch(character -> Character.isISOControl(character))) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_HEADER_INVALID", "The webhook " + name + " contains control characters.");
    }
    return candidate;
  }

  private Instant parseTimestamp(String value) {
    try {
      return Instant.parse(value);
    } catch (DateTimeException exception) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_TIMESTAMP_INVALID", "The webhook delivery timestamp must be RFC 3339 UTC.");
    }
  }

  private void validateTimestampTolerance(Instant deliveryTimestamp, int toleranceSeconds) {
    Instant now = clock.instant();
    if (deliveryTimestamp.isBefore(now.minusSeconds(toleranceSeconds))
        || deliveryTimestamp.isAfter(now.plusSeconds(toleranceSeconds))) {
      throw WebhookIngestionException.unauthorized(
          "WEBHOOK_TIMESTAMP_OUTSIDE_TOLERANCE",
          "The webhook delivery timestamp is outside the permitted tolerance.");
    }
  }

  private byte[] readBoundedPayload(
      InputStream requestBody, long contentLength, int maximumSizeBytes) {
    if (contentLength > maximumSizeBytes) {
      throw WebhookIngestionException.payloadTooLarge();
    }
    try {
      byte[] payload = requestBody.readNBytes(maximumSizeBytes + 1);
      if (payload.length > maximumSizeBytes) {
        throw WebhookIngestionException.payloadTooLarge();
      }
      if (payload.length == 0) {
        throw WebhookIngestionException.badRequest(
            "WEBHOOK_PAYLOAD_EMPTY", "The webhook payload must not be empty.");
      }
      return payload;
    } catch (IOException exception) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_PAYLOAD_UNREADABLE", "The webhook payload could not be read.");
    }
  }

  private void validateJson(byte[] payload) {
    try {
      objectMapper.readTree(payload);
    } catch (JsonProcessingException exception) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_JSON_INVALID", "The webhook payload must contain valid JSON.");
    } catch (IOException exception) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_PAYLOAD_UNREADABLE", "The webhook payload could not be read.");
    }
  }
}
