package com.samharrison.incidentresponse.retrieval;

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
@Table(name = "historical_retrieval_records")
public class HistoricalRetrievalRecord {

  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Column(name = "incident_id")
  private UUID incidentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_kind", nullable = false, length = 32)
  private HistoricalSourceKind sourceKind;

  @Column(name = "source_id", nullable = false)
  private UUID sourceId;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(length = 32)
  private String provider;

  @Column(name = "pipeline_name", length = 200)
  private String pipelineName;

  @Column(name = "environment_name", length = 120)
  private String environmentName;

  @Column(name = "git_ref", length = 500)
  private String gitRef;

  @Column(name = "commit_sha", length = 64)
  private String commitSha;

  @Column(name = "diagnosis_category", length = 80)
  private String diagnosisCategory;

  @Column(nullable = false, length = 2000)
  private String summary;

  @Column(name = "match_explanation", nullable = false, length = 500)
  private String matchExplanation;

  @Column(name = "provenance_reference", nullable = false, length = 200)
  private String provenanceReference;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected HistoricalRetrievalRecord() {}

  public HistoricalRetrievalRecord(
      UUID id,
      UUID organisationId,
      UUID projectId,
      UUID incidentId,
      HistoricalSourceKind sourceKind,
      UUID sourceId,
      Instant occurredAt,
      String provider,
      String pipelineName,
      String environmentName,
      String gitRef,
      String commitSha,
      String diagnosisCategory,
      String summary,
      String matchExplanation,
      String provenanceReference,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.incidentId = incidentId;
    this.sourceKind = Objects.requireNonNull(sourceKind);
    this.sourceId = Objects.requireNonNull(sourceId);
    this.occurredAt = Objects.requireNonNull(occurredAt);
    this.provider = bounded(provider, 32, "provider");
    this.pipelineName = bounded(pipelineName, 200, "pipelineName");
    this.environmentName = bounded(environmentName, 120, "environmentName");
    this.gitRef = bounded(gitRef, 500, "gitRef");
    this.commitSha = bounded(commitSha, 64, "commitSha");
    this.diagnosisCategory = bounded(diagnosisCategory, 80, "diagnosisCategory");
    this.summary = requiredBounded(summary, 2000, "summary");
    this.matchExplanation = requiredBounded(matchExplanation, 500, "matchExplanation");
    this.provenanceReference = requiredBounded(provenanceReference, 200, "provenanceReference");
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

  public HistoricalSourceKind getSourceKind() {
    return sourceKind;
  }

  public UUID getSourceId() {
    return sourceId;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public String getProvider() {
    return provider;
  }

  public String getPipelineName() {
    return pipelineName;
  }

  public String getEnvironmentName() {
    return environmentName;
  }

  public String getGitRef() {
    return gitRef;
  }

  public String getCommitSha() {
    return commitSha;
  }

  public String getDiagnosisCategory() {
    return diagnosisCategory;
  }

  public String getSummary() {
    return summary;
  }

  public String getMatchExplanation() {
    return matchExplanation;
  }

  public String getProvenanceReference() {
    return provenanceReference;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  private static String bounded(String value, int max, String field) {
    if (value == null) {
      return null;
    }
    if (value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException(field + " is outside the permitted range");
    }
    return value;
  }

  private static String requiredBounded(String value, int max, String field) {
    if (value == null || value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException(field + " is outside the permitted range");
    }
    return value;
  }
}
