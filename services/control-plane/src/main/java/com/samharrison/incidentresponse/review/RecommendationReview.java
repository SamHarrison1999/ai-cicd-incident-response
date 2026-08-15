package com.samharrison.incidentresponse.review;

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
@Table(name = "recommendation_reviews")
public class RecommendationReview {
  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "recommendation_id", nullable = false)
  private UUID recommendationId;

  @Column(name = "reviewed_version_id")
  private UUID reviewedVersionId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ReviewAction action;

  @Enumerated(EnumType.STRING)
  @Column(name = "reason_category", nullable = false, length = 32)
  private ReviewReason reasonCategory;

  @Column(length = 500)
  private String comment;

  @Column(name = "reviewer_user_id", nullable = false)
  private UUID reviewerUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected RecommendationReview() {}

  public RecommendationReview(
      UUID id,
      UUID organisationId,
      UUID projectId,
      UUID recommendationId,
      UUID reviewedVersionId,
      ReviewAction action,
      ReviewReason reasonCategory,
      String comment,
      UUID reviewerUserId,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.recommendationId = Objects.requireNonNull(recommendationId);
    this.reviewedVersionId = reviewedVersionId;
    this.action = Objects.requireNonNull(action);
    this.reasonCategory = Objects.requireNonNull(reasonCategory);
    this.comment = optional(comment, 500);
    this.reviewerUserId = Objects.requireNonNull(reviewerUserId);
    this.createdAt = Objects.requireNonNull(createdAt);
    if (action == ReviewAction.REJECT && reasonCategory == ReviewReason.NONE) {
      throw new IllegalArgumentException("rejected reviews require a reason category");
    }
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

  public UUID getReviewedVersionId() {
    return reviewedVersionId;
  }

  public ReviewAction getAction() {
    return action;
  }

  public ReviewReason getReasonCategory() {
    return reasonCategory;
  }

  public String getComment() {
    return comment;
  }

  public UUID getReviewerUserId() {
    return reviewerUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  private static String optional(String value, int max) {
    if (value == null) {
      return null;
    }
    if (value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException("comment is outside the permitted range");
    }
    return value;
  }
}
