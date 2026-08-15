package com.samharrison.incidentresponse.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "recommendation_citations")
public class RecommendationCitation {

  @Id private UUID id;

  @Column(name = "recommendation_id", nullable = false)
  private UUID recommendationId;

  @Column(name = "evidence_id")
  private UUID evidenceId;

  @Column(name = "historical_record_id")
  private UUID historicalRecordId;

  @Column(nullable = false, length = 500)
  private String claim;

  protected RecommendationCitation() {}

  public RecommendationCitation(
      UUID id, UUID recommendationId, UUID evidenceId, UUID historicalRecordId, String claim) {
    this.id = Objects.requireNonNull(id);
    this.recommendationId = Objects.requireNonNull(recommendationId);
    if ((evidenceId == null) == (historicalRecordId == null)) {
      throw new IllegalArgumentException("exactly one citation source is required");
    }
    this.evidenceId = evidenceId;
    this.historicalRecordId = historicalRecordId;
    this.claim = required(claim);
  }

  public UUID getId() {
    return id;
  }

  public UUID getRecommendationId() {
    return recommendationId;
  }

  public UUID getEvidenceId() {
    return evidenceId;
  }

  public UUID getHistoricalRecordId() {
    return historicalRecordId;
  }

  public String getClaim() {
    return claim;
  }

  private static String required(String value) {
    if (value == null || value.isBlank() || value.length() > 500) {
      throw new IllegalArgumentException("claim is outside the permitted range");
    }
    return value;
  }
}
