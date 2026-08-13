package com.samharrison.incidentresponse.ingestion;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/event-sources/{eventSourceId}/deliveries")
public class WebhookIngestionController {

  private final WebhookIngestionService ingestionService;

  public WebhookIngestionController(WebhookIngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  @PostMapping
  ResponseEntity<WebhookAcceptanceResponse> ingest(
      @PathVariable UUID eventSourceId,
      @RequestHeader(value = WebhookHeaders.DELIVERY_ID, required = false) String deliveryId,
      @RequestHeader(value = WebhookHeaders.EVENT_TYPE, required = false) String eventType,
      @RequestHeader(value = WebhookHeaders.DELIVERY_TIMESTAMP, required = false)
          String deliveryTimestamp,
      @RequestHeader(value = WebhookHeaders.SIGNATURE, required = false) String signature,
      HttpServletRequest request) {
    try {
      WebhookAcceptanceResponse response =
          ingestionService.ingest(
              eventSourceId,
              deliveryId,
              eventType,
              deliveryTimestamp,
              signature,
              request.getContentType(),
              request.getContentLengthLong(),
              request.getInputStream());
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    } catch (IOException exception) {
      throw WebhookIngestionException.badRequest(
          "WEBHOOK_PAYLOAD_UNREADABLE", "The webhook payload could not be read.");
    }
  }
}
