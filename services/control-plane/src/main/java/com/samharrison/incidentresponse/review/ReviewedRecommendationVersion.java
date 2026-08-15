package com.samharrison.incidentresponse.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "reviewed_recommendation_versions")
public class ReviewedRecommendationVersion {
  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "recommendation_id", nullable = false)
  private UUID recommendationId;

  @Column(name = "version_number", nullable = false)
  private int versionNumber;

  @Column(nullable = false, length = 2000)
  private String summary;

  @Column(name = "likely_cause", length = 1000)
  private String likelyCause;

  @Column(name = "reviewer_user_id", nullable = false)
  private UUID reviewerUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected ReviewedRecommendationVersion() {}

  public ReviewedRecommendationVersion(
      UUID id,
      UUID organisationId,
      UUID projectId,
      UUID recommendationId,
      int versionNumber,
      String summary,
      String likelyCause,
      UUID reviewerUserId,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.recommendationId = Objects.requireNonNull(recommendationId);
    if (versionNumber <= 0) {
      throw new IllegalArgumentException("versionNumber must be positive");
    }
    this.versionNumber = versionNumber;
    this.summary = required(summary, 2000);
    this.likelyCause = optional(likelyCause, 1000);
    this.reviewerUserId = Objects.requireNonNull(reviewerUserId);
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

  public UUID getRecommendationId() {
    return recommendationId;
  }

  public int getVersionNumber() {
    return versionNumber;
  }

  public String getSummary() {
    return summary;
  }

  public String getLikelyCause() {
    return likelyCause;
  }

  public UUID getReviewerUserId() {
    return reviewerUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  private static String required(String value, int max) {
    if (value == null || value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException("summary is outside the permitted range");
    }
    return value;
  }

  private static String optional(String value, int max) {
    if (value == null) {
      return null;
    }
    if (value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException("likelyCause is outside the permitted range");
    }
    return value;
  }
}
