package com.samharrison.incidentresponse.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "incident_correlation_decisions")
public class IncidentCorrelationDecisionRecord {

  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "event_id", nullable = false, unique = true)
  private UUID eventId;

  @Column(name = "incident_id")
  private UUID incidentId;

  @Column(name = "policy_version", nullable = false, length = 80)
  private String policyVersion;

  @Column(nullable = false)
  private int score;

  @Column(nullable = false)
  private int threshold;

  @Column(name = "matched_dimensions", columnDefinition = "jsonb", nullable = false)
  private String matchedDimensions;

  @Column(name = "considered_candidates", columnDefinition = "jsonb", nullable = false)
  private String consideredCandidates;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected IncidentCorrelationDecisionRecord() {}

  public IncidentCorrelationDecisionRecord(
      UUID id,
      UUID organisationId,
      UUID projectId,
      CorrelationDecision decision,
      String matchedDimensions,
      String consideredCandidates,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.eventId = Objects.requireNonNull(decision.eventId());
    this.incidentId = decision.selectedIncidentId();
    this.policyVersion = Objects.requireNonNull(decision.policyVersion());
    this.score = decision.score();
    this.threshold = decision.threshold();
    this.matchedDimensions = requireText(matchedDimensions, "matchedDimensions");
    this.consideredCandidates = requireText(consideredCandidates, "consideredCandidates");
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

  public UUID getEventId() {
    return eventId;
  }

  public UUID getIncidentId() {
    return incidentId;
  }

  public String getPolicyVersion() {
    return policyVersion;
  }

  public int getScore() {
    return score;
  }

  public int getThreshold() {
    return threshold;
  }

  public String getMatchedDimensions() {
    return matchedDimensions;
  }

  public String getConsideredCandidates() {
    return consideredCandidates;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
