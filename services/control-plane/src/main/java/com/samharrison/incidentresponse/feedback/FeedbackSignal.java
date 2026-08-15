package com.samharrison.incidentresponse.feedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "feedback_signals")
public class FeedbackSignal {
  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "recommendation_id", nullable = false)
  private UUID recommendationId;

  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private FeedbackOutcome outcome;

  @Column(name = "policy_version", nullable = false, length = 64)
  private String policyVersion;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected FeedbackSignal() {}

  public FeedbackSignal(
      UUID id,
      UUID organisationId,
      UUID projectId,
      UUID recommendationId,
      UUID reviewId,
      FeedbackOutcome outcome,
      String policyVersion,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.recommendationId = Objects.requireNonNull(recommendationId);
    this.reviewId = Objects.requireNonNull(reviewId);
    this.outcome = Objects.requireNonNull(outcome);
    this.policyVersion = required(policyVersion, 64);
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

  public UUID getReviewId() {
    return reviewId;
  }

  public FeedbackOutcome getOutcome() {
    return outcome;
  }

  public String getPolicyVersion() {
    return policyVersion;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  private static String required(String value, int max) {
    if (value == null || value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException("policyVersion is outside the permitted range");
    }
    return value;
  }
}
