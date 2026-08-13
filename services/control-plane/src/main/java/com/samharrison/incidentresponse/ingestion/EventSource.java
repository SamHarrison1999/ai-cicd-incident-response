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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "event_sources",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_event_source_project_display_name",
            columnNames = {"project_id", "display_name"}))
public class EventSource {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organisation_id", nullable = false)
  private Organisation organisation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private EventProvider provider;

  @Column(name = "display_name", nullable = false, length = 120)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private EventSourceStatus status;

  @Column(name = "signing_secret_reference", nullable = false, length = 255)
  private String signingSecretReference;

  @Enumerated(EnumType.STRING)
  @Column(name = "signature_algorithm", nullable = false, length = 32)
  private SignatureAlgorithm signatureAlgorithm;

  @Column(name = "timestamp_tolerance_seconds", nullable = false)
  private int timestampToleranceSeconds;

  @Column(name = "max_payload_size_bytes", nullable = false)
  private int maxPayloadSizeBytes;

  @Column(name = "secret_rotated_at")
  private Instant secretRotatedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected EventSource() {}

  public EventSource(
      UUID id,
      Organisation organisation,
      Project project,
      EventProvider provider,
      String displayName,
      EventSourceStatus status,
      String signingSecretReference,
      SignatureAlgorithm signatureAlgorithm,
      int timestampToleranceSeconds,
      int maxPayloadSizeBytes) {
    this.id = Objects.requireNonNull(id);
    this.organisation = Objects.requireNonNull(organisation);
    this.project = Objects.requireNonNull(project);
    this.provider = Objects.requireNonNull(provider);
    this.displayName = requireText(displayName, "displayName");
    this.status = Objects.requireNonNull(status);
    this.signingSecretReference = requireText(signingSecretReference, "signingSecretReference");
    this.signatureAlgorithm = Objects.requireNonNull(signatureAlgorithm);
    this.timestampToleranceSeconds =
        requirePositive(timestampToleranceSeconds, "timestampToleranceSeconds");
    this.maxPayloadSizeBytes = requirePositive(maxPayloadSizeBytes, "maxPayloadSizeBytes");
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
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

  public EventProvider getProvider() {
    return provider;
  }

  public String getDisplayName() {
    return displayName;
  }

  public EventSourceStatus getStatus() {
    return status;
  }

  public String getSigningSecretReference() {
    return signingSecretReference;
  }

  public SignatureAlgorithm getSignatureAlgorithm() {
    return signatureAlgorithm;
  }

  public int getTimestampToleranceSeconds() {
    return timestampToleranceSeconds;
  }

  public int getMaxPayloadSizeBytes() {
    return maxPayloadSizeBytes;
  }

  public Instant getSecretRotatedAt() {
    return secretRotatedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public long getVersion() {
    return version;
  }

  public boolean isEnabled() {
    return status == EventSourceStatus.ENABLED;
  }

  public void disable() {
    status = EventSourceStatus.DISABLED;
  }

  public void enable() {
    status = EventSourceStatus.ENABLED;
  }

  public void rotateSecretReference(String reference, Instant rotatedAt) {
    signingSecretReference = requireText(reference, "signingSecretReference");
    secretRotatedAt = Objects.requireNonNull(rotatedAt);
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }

  private static int requirePositive(int value, String fieldName) {
    if (value <= 0) {
      throw new IllegalArgumentException(fieldName + " must be positive");
    }
    return value;
  }
}
