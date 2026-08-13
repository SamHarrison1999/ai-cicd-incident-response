package com.samharrison.incidentresponse.ingestion;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WebhookDeliveryStore {

  private static final String INSERT_DELIVERY =
      """
      INSERT INTO webhook_deliveries (
          id,
          organisation_id,
          project_id,
          event_source_id,
          provider_delivery_id,
          provider_event_type,
          payload_sha256,
          delivery_timestamp,
          received_at,
          status,
          version
      ) VALUES (
          :id,
          :organisationId,
          :projectId,
          :eventSourceId,
          :providerDeliveryId,
          :providerEventType,
          :payloadSha256,
          :deliveryTimestamp,
          :receivedAt,
          'RECEIVED',
          0
      )
      ON CONFLICT (event_source_id, provider_delivery_id) DO NOTHING
      """;

  private final JdbcClient jdbcClient;
  private final WebhookDeliveryRepository deliveryRepository;

  public WebhookDeliveryStore(JdbcClient jdbcClient, WebhookDeliveryRepository deliveryRepository) {
    this.jdbcClient = jdbcClient;
    this.deliveryRepository = deliveryRepository;
  }

  @Transactional
  public StoredWebhookDelivery store(
      EventSource eventSource,
      String providerDeliveryId,
      String providerEventType,
      String payloadSha256,
      Instant deliveryTimestamp,
      Instant receivedAt) {
    UUID candidateId = UUID.randomUUID();
    int inserted =
        jdbcClient
            .sql(INSERT_DELIVERY)
            .params(
                Map.of(
                    "id", candidateId,
                    "organisationId", eventSource.getOrganisation().getId(),
                    "projectId", eventSource.getProject().getId(),
                    "eventSourceId", eventSource.getId(),
                    "providerDeliveryId", providerDeliveryId,
                    "providerEventType", providerEventType,
                    "payloadSha256", payloadSha256,
                    "deliveryTimestamp", Timestamp.from(deliveryTimestamp),
                    "receivedAt", Timestamp.from(receivedAt)))
            .update();

    WebhookDelivery stored =
        deliveryRepository
            .findByEventSourceIdAndProviderDeliveryId(eventSource.getId(), providerDeliveryId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "The webhook delivery insert completed without a readable record."));

    if (!stored.getProviderEventType().equals(providerEventType)
        || !stored.getPayloadSha256().equals(payloadSha256)) {
      throw WebhookIngestionException.conflict();
    }

    return new StoredWebhookDelivery(stored, inserted == 0);
  }

  public record StoredWebhookDelivery(WebhookDelivery delivery, boolean duplicate) {}
}
