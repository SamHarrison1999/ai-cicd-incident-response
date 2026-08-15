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
@Table(name = "feedback_aggregates")
public class FeedbackAggregate {
  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "policy_version", nullable = false, length = 64)
  private String policyVersion;

  @Column(name = "window_start", nullable = false)
  private Instant windowStart;

  @Column(name = "window_end", nullable = false)
  private Instant windowEnd;

  @Column(name = "sample_count", nullable = false)
  private int sampleCount;

  @Column(name = "accepted_count", nullable = false)
  private int acceptedCount;

  @Column(name = "edited_count", nullable = false)
  private int editedCount;

  @Column(name = "rejected_count", nullable = false)
  private int rejectedCount;

  @Column(name = "resolved_count", nullable = false)
  private int resolvedCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "suppression_reason", nullable = false, length = 32)
  private FeedbackSuppressionReason suppressionReason;

  protected FeedbackAggregate() {}

  public FeedbackAggregate(
      UUID id,
      UUID organisationId,
      UUID projectId,
      String policyVersion,
      Instant windowStart,
      Instant windowEnd,
      int sampleCount,
      int acceptedCount,
      int editedCount,
      int rejectedCount,
      int resolvedCount,
      FeedbackSuppressionReason suppressionReason) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.policyVersion = Objects.requireNonNull(policyVersion);
    this.windowStart = Objects.requireNonNull(windowStart);
    this.windowEnd = Objects.requireNonNull(windowEnd);
    if (windowEnd.isBefore(windowStart)
        || sampleCount < 0
        || acceptedCount < 0
        || editedCount < 0
        || rejectedCount < 0
        || resolvedCount < 0) {
      throw new IllegalArgumentException("feedback aggregate bounds are invalid");
    }
    this.sampleCount = sampleCount;
    this.acceptedCount = acceptedCount;
    this.editedCount = editedCount;
    this.rejectedCount = rejectedCount;
    this.resolvedCount = resolvedCount;
    this.suppressionReason = Objects.requireNonNull(suppressionReason);
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

  public String getPolicyVersion() {
    return policyVersion;
  }

  public Instant getWindowStart() {
    return windowStart;
  }

  public Instant getWindowEnd() {
    return windowEnd;
  }

  public int getSampleCount() {
    return sampleCount;
  }

  public int getAcceptedCount() {
    return acceptedCount;
  }

  public int getEditedCount() {
    return editedCount;
  }

  public int getRejectedCount() {
    return rejectedCount;
  }

  public int getResolvedCount() {
    return resolvedCount;
  }

  public FeedbackSuppressionReason getSuppressionReason() {
    return suppressionReason;
  }
}
