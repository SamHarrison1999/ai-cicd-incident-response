package com.samharrison.incidentresponse.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "incident_resolutions")
public class IncidentResolution {
  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "incident_id", nullable = false)
  private UUID incidentId;

  @Column(name = "recommendation_id", nullable = false)
  private UUID recommendationId;

  @Column(name = "reviewed_version_id", nullable = false)
  private UUID reviewedVersionId;

  @Column(name = "resolution_text", nullable = false, length = 2000)
  private String resolutionText;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected IncidentResolution() {}

  public IncidentResolution(
      UUID id,
      UUID organisationId,
      UUID projectId,
      UUID incidentId,
      UUID recommendationId,
      UUID reviewedVersionId,
      String resolutionText,
      UUID createdBy,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.incidentId = Objects.requireNonNull(incidentId);
    this.recommendationId = Objects.requireNonNull(recommendationId);
    if (reviewedVersionId == null) {
      throw new IllegalArgumentException("reviewedVersionId is required");
    }
    this.reviewedVersionId = reviewedVersionId;
    if (resolutionText == null || resolutionText.isBlank() || resolutionText.length() > 2000) {
      throw new IllegalArgumentException("resolutionText is outside the permitted range");
    }
    this.resolutionText = resolutionText;
    this.createdBy = Objects.requireNonNull(createdBy);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getOrganisationId() {
    return organisationId;
  }

  public UUID getProjectId() {
    return projectId;
  }

  public UUID getIncidentId() {
    return incidentId;
  }

  public UUID getRecommendationId() {
    return recommendationId;
  }

  public UUID getReviewedVersionId() {
    return reviewedVersionId;
  }

  public String getResolutionText() {
    return resolutionText;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
