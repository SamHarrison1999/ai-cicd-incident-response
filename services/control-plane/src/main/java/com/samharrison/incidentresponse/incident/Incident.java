package com.samharrison.incidentresponse.incident;

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
    name = "incidents",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_incident_id_project_organisation",
            columnNames = {"id", "project_id", "organisation_id"}))
public class Incident {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organisation_id", nullable = false)
  private Organisation organisation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private IncidentStatus status;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 1000)
  private String summary;

  @Column(name = "detected_at", nullable = false)
  private Instant detectedAt;

  @Column(name = "resolved_at")
  private Instant resolvedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected Incident() {}

  public Incident(
      UUID id,
      Organisation organisation,
      Project project,
      String title,
      String summary,
      Instant detectedAt) {
    this.id = Objects.requireNonNull(id);
    this.organisation = Objects.requireNonNull(organisation);
    this.project = Objects.requireNonNull(project);
    this.status = IncidentStatus.DETECTED;
    this.title = requireText(title, "title");
    this.summary = requireText(summary, "summary");
    this.detectedAt = Objects.requireNonNull(detectedAt);
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

  public IncidentStatus getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public String getSummary() {
    return summary;
  }

  public Instant getDetectedAt() {
    return detectedAt;
  }

  public Instant getResolvedAt() {
    return resolvedAt;
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

  public void transitionTo(IncidentStatus nextStatus, Instant occurredAt) {
    Objects.requireNonNull(nextStatus);
    Objects.requireNonNull(occurredAt);
    if (!isAllowed(status, nextStatus)) {
      throw new IllegalStateException(
          "incident transition is not allowed: " + status + " -> " + nextStatus);
    }
    status = nextStatus;
    updatedAt = occurredAt;
    if (nextStatus == IncidentStatus.RESOLVED) {
      resolvedAt = occurredAt;
    } else if (nextStatus == IncidentStatus.REOPENED) {
      resolvedAt = null;
    }
  }

  private static boolean isAllowed(IncidentStatus current, IncidentStatus next) {
    return switch (current) {
      case DETECTED -> next == IncidentStatus.TRIAGED || next == IncidentStatus.RESOLVED;
      case TRIAGED -> next == IncidentStatus.MITIGATING || next == IncidentStatus.RESOLVED;
      case MITIGATING -> next == IncidentStatus.MONITORING;
      case MONITORING -> next == IncidentStatus.RESOLVED || next == IncidentStatus.MITIGATING;
      case RESOLVED -> next == IncidentStatus.REOPENED;
      case REOPENED -> next == IncidentStatus.TRIAGED;
    };
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
