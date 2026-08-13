package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "webhook_deliveries",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_webhook_delivery_source_provider_id",
            columnNames = {"event_source_id", "provider_delivery_id"}))
public class WebhookDelivery {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organisation_id", nullable = false)
  private Organisation organisation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_source_id", nullable = false)
  private EventSource eventSource;

  @Column(name = "provider_delivery_id", nullable = false, length = 200)
  private String providerDeliveryId;

  @Column(name = "provider_event_type", nullable = false, length = 100)
  private String providerEventType;

  @Column(name = "payload_sha256", nullable = false, length = 64)
  private String payloadSha256;

  @Column(name = "delivery_timestamp", nullable = false)
  private Instant deliveryTimestamp;

  @Column(name = "received_at", nullable = false, updatable = false)
  private Instant receivedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private WebhookDeliveryStatus status;

  @Column(name = "outcome_code", length = 80)
  private String outcomeCode;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected WebhookDelivery() {}

  public WebhookDelivery(
      UUID id,
      Organisation organisation,
      Project project,
      EventSource eventSource,
      String providerDeliveryId,
      String providerEventType,
      String payloadSha256,
      Instant deliveryTimestamp,
      Instant receivedAt) {
    this.id = Objects.requireNonNull(id);
    this.organisation = Objects.requireNonNull(organisation);
    this.project = Objects.requireNonNull(project);
    this.eventSource = Objects.requireNonNull(eventSource);
    this.providerDeliveryId = requireText(providerDeliveryId, "providerDeliveryId");
    this.providerEventType = requireText(providerEventType, "providerEventType");
    this.payloadSha256 = requireSha256(payloadSha256);
    this.deliveryTimestamp = Objects.requireNonNull(deliveryTimestamp);
    this.receivedAt = Objects.requireNonNull(receivedAt);
    this.status = WebhookDeliveryStatus.RECEIVED;
  }

  public UUID getId() {
    return id;
  }

  public Organisation getOrganisation() {
    return organisation;
  }

  public Project getProject() {
    return project;
  }

  public EventSource getEventSource() {
    return eventSource;
  }

  public String getProviderDeliveryId() {
    return providerDeliveryId;
  }

  public String getProviderEventType() {
    return providerEventType;
  }

  public String getPayloadSha256() {
    return payloadSha256;
  }

  public Instant getDeliveryTimestamp() {
    return deliveryTimestamp;
  }

  public Instant getReceivedAt() {
    return receivedAt;
  }

  public WebhookDeliveryStatus getStatus() {
    return status;
  }

  public String getOutcomeCode() {
    return outcomeCode;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }

  public long getVersion() {
    return version;
  }

  public void markProcessed(String code, Instant at) {
    complete(WebhookDeliveryStatus.PROCESSED, code, at);
  }

  public void markRejected(String code, Instant at) {
    complete(WebhookDeliveryStatus.REJECTED, code, at);
  }

  public void markFailed(String code, Instant at) {
    complete(WebhookDeliveryStatus.FAILED, code, at);
  }

  public void markProcessingRetry() {
    if (status != WebhookDeliveryStatus.FAILED) {
      throw new IllegalStateException("only failed deliveries can be retried");
    }
    status = WebhookDeliveryStatus.PROCESSING_RETRY;
    outcomeCode = null;
    processedAt = null;
  }

  private void complete(WebhookDeliveryStatus newStatus, String code, Instant at) {
    status = Objects.requireNonNull(newStatus);
    outcomeCode = requireText(code, "outcomeCode");
    processedAt = Objects.requireNonNull(at);
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }

  private static String requireSha256(String value) {
    String hash = requireText(value, "payloadSha256");
    if (!hash.matches("[0-9a-f]{64}")) {
      throw new IllegalArgumentException(
          "payloadSha256 must be 64 lowercase hexadecimal characters");
    }
    return hash;
  }
}
