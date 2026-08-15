package com.samharrison.incidentresponse.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "recommendations")
public class Recommendation {

  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "incident_id")
  private UUID incidentId;

  @Column(nullable = false, length = 80)
  private String category;

  @Column(nullable = false, length = 2000)
  private String summary;

  @Column(name = "likely_cause", length = 1000)
  private String likelyCause;

  @Column(nullable = false, precision = 4, scale = 3)
  private BigDecimal confidence;

  @Column(name = "confidence_explanation", nullable = false, length = 500)
  private String confidenceExplanation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private RecommendationStatus status;

  @Column(name = "abstention_reason", length = 500)
  private String abstentionReason;

  @Column(name = "provider_name", nullable = false, length = 80)
  private String providerName;

  @Column(name = "model_version", nullable = false, length = 80)
  private String modelVersion;

  @Column(name = "prompt_template_version", nullable = false, length = 80)
  private String promptTemplateVersion;

  @Column(name = "ruleset_version", nullable = false, length = 80)
  private String rulesetVersion;

  @Column(name = "retrieval_set_version", nullable = false, length = 80)
  private String retrievalSetVersion;

  @Column(name = "schema_version", nullable = false, length = 32)
  private String schemaVersion;

  @Column(name = "generated_at", nullable = false)
  private Instant generatedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Recommendation() {}

  public Recommendation(
      UUID id,
      UUID organisationId,
      UUID projectId,
      UUID incidentId,
      String category,
      String summary,
      String likelyCause,
      BigDecimal confidence,
      String confidenceExplanation,
      RecommendationStatus status,
      String abstentionReason,
      String providerName,
      String modelVersion,
      String promptTemplateVersion,
      String rulesetVersion,
      String retrievalSetVersion,
      String schemaVersion,
      Instant generatedAt) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.incidentId = incidentId;
    this.category = required(category, 80, "category");
    this.summary = required(summary, 2000, "summary");
    this.likelyCause = optional(likelyCause, 1000, "likelyCause");
    this.confidence = Objects.requireNonNull(confidence);
    if (confidence.compareTo(BigDecimal.ZERO) < 0 || confidence.compareTo(BigDecimal.ONE) > 0) {
      throw new IllegalArgumentException("confidence must be between zero and one");
    }
    this.confidenceExplanation = required(confidenceExplanation, 500, "confidenceExplanation");
    this.status = Objects.requireNonNull(status);
    this.abstentionReason = optional(abstentionReason, 500, "abstentionReason");
    this.providerName = required(providerName, 80, "providerName");
    this.modelVersion = required(modelVersion, 80, "modelVersion");
    this.promptTemplateVersion = required(promptTemplateVersion, 80, "promptTemplateVersion");
    this.rulesetVersion = required(rulesetVersion, 80, "rulesetVersion");
    this.retrievalSetVersion = required(retrievalSetVersion, 80, "retrievalSetVersion");
    this.schemaVersion = required(schemaVersion, 32, "schemaVersion");
    this.generatedAt = Objects.requireNonNull(generatedAt);
    this.createdAt = generatedAt;
    if (status == RecommendationStatus.ABSTAINED && this.abstentionReason == null) {
      throw new IllegalArgumentException("abstentionReason is required for abstained results");
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

  public UUID getIncidentId() {
    return incidentId;
  }

  public String getCategory() {
    return category;
  }

  public String getSummary() {
    return summary;
  }

  public String getLikelyCause() {
    return likelyCause;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public String getConfidenceExplanation() {
    return confidenceExplanation;
  }

  public RecommendationStatus getStatus() {
    return status;
  }

  public String getAbstentionReason() {
    return abstentionReason;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getModelVersion() {
    return modelVersion;
  }

  public String getPromptTemplateVersion() {
    return promptTemplateVersion;
  }

  public String getRulesetVersion() {
    return rulesetVersion;
  }

  public String getRetrievalSetVersion() {
    return retrievalSetVersion;
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }

  public Instant getGeneratedAt() {
    return generatedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  private static String required(String value, int max, String field) {
    if (value == null || value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException(field + " is outside the permitted range");
    }
    return value;
  }

  private static String optional(String value, int max, String field) {
    if (value == null) {
      return null;
    }
    if (value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException(field + " is outside the permitted range");
    }
    return value;
  }
}
