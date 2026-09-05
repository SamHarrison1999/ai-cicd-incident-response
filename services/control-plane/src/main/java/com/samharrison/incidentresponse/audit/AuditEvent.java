package com.samharrison.incidentresponse.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

  @Id private UUID id;

  @Column(name = "actor_user_id")
  private UUID actorUserId;

  @Column(name = "organisation_id")
  private UUID organisationId;

  @Column(nullable = false, length = 120)
  private String action;

  @Column(name = "target_type", nullable = false, length = 80)
  private String targetType;

  @Column(name = "target_id")
  private UUID targetId;

  @Column(name = "correlation_id", nullable = false, length = 128)
  private String correlationId;

  @Column(columnDefinition = "jsonb", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String metadata;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  protected AuditEvent() {}

  public AuditEvent(
      UUID id,
      UUID actorUserId,
      UUID organisationId,
      String action,
      String targetType,
      UUID targetId,
      String correlationId,
      String metadata,
      Instant occurredAt) {
    this.id = Objects.requireNonNull(id);
    this.actorUserId = actorUserId;
    this.organisationId = organisationId;
    this.action = requireText(action, "action");
    this.targetType = requireText(targetType, "targetType");
    this.targetId = targetId;
    this.correlationId = requireText(correlationId, "correlationId");
    this.metadata = requireText(metadata, "metadata");
    this.occurredAt = Objects.requireNonNull(occurredAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getActorUserId() {
    return actorUserId;
  }

  public UUID getOrganisationId() {
    return organisationId;
  }

  public String getAction() {
    return action;
  }

  public String getTargetType() {
    return targetType;
  }

  public UUID getTargetId() {
    return targetId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public String getMetadata() {
    return metadata;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
