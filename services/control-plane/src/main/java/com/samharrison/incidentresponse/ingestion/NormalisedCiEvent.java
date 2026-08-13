package com.samharrison.incidentresponse.ingestion;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "normalised_ci_events")
public class NormalisedCiEvent {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organisation_id", nullable = false)
  private Organisation organisation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_source_id", nullable = false)
  private EventSource eventSource;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "webhook_delivery_id", nullable = false, unique = true)
  private WebhookDelivery webhookDelivery;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pipeline_run_id")
  private PipelineRun pipelineRun;

  @Column(name = "schema_version", nullable = false, length = 16)
  private String schemaVersion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private EventProvider provider;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 64)
  private NormalisedEventType eventType;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;

  @Column(name = "external_run_id", nullable = false, length = 200)
  private String externalRunId;

  @Column(name = "pipeline_name", nullable = false, length = 200)
  private String pipelineName;

  @Column(name = "run_attempt", nullable = false)
  private int runAttempt;

  @Enumerated(EnumType.STRING)
  @Column(name = "pipeline_status", nullable = false, length = 32)
  private PipelineRunStatus pipelineStatus;

  @Column(name = "commit_sha", length = 64)
  private String commitSha;

  @Column(name = "git_ref", length = 500)
  private String gitRef;

  @Column(name = "environment_name", length = 120)
  private String environmentName;

  @Column(name = "evidence_summary", nullable = false, length = 1000)
  private String evidenceSummary;

  @Column(name = "source_fields", columnDefinition = "jsonb", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private List<String> sourceFields;

  protected NormalisedCiEvent() {}

  public NormalisedCiEvent(
      UUID id,
      Organisation organisation,
      Project project,
      EventSource eventSource,
      WebhookDelivery webhookDelivery,
      PipelineRun pipelineRun,
      String schemaVersion,
      EventProvider provider,
      NormalisedEventType eventType,
      Instant occurredAt,
      Instant receivedAt,
      String externalRunId,
      String pipelineName,
      int runAttempt,
      PipelineRunStatus pipelineStatus,
      String commitSha,
      String gitRef,
      String environmentName,
      String evidenceSummary,
      List<String> sourceFields) {
    this.id = Objects.requireNonNull(id);
    this.organisation = Objects.requireNonNull(organisation);
    this.project = Objects.requireNonNull(project);
    this.eventSource = Objects.requireNonNull(eventSource);
    this.webhookDelivery = Objects.requireNonNull(webhookDelivery);
    this.pipelineRun = pipelineRun;
    this.schemaVersion = requireText(schemaVersion, "schemaVersion");
    this.provider = Objects.requireNonNull(provider);
    this.eventType = Objects.requireNonNull(eventType);
    this.occurredAt = Objects.requireNonNull(occurredAt);
    this.receivedAt = Objects.requireNonNull(receivedAt);
    this.externalRunId = requireText(externalRunId, "externalRunId");
    this.pipelineName = requireText(pipelineName, "pipelineName");
    this.runAttempt = requirePositive(runAttempt, "runAttempt");
    this.pipelineStatus = Objects.requireNonNull(pipelineStatus);
    this.commitSha = commitSha;
    this.gitRef = gitRef;
    this.environmentName = environmentName;
    this.evidenceSummary = requireText(evidenceSummary, "evidenceSummary");
    this.sourceFields = requireSourceFields(sourceFields);
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

  public EventSource getEventSource() {
    return eventSource;
  }

  public WebhookDelivery getWebhookDelivery() {
    return webhookDelivery;
  }

  public PipelineRun getPipelineRun() {
    return pipelineRun;
  }

  public String getSchemaVersion() {
    return schemaVersion;
  }

  public EventProvider getProvider() {
    return provider;
  }

  public NormalisedEventType getEventType() {
    return eventType;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public Instant getReceivedAt() {
    return receivedAt;
  }

  public String getExternalRunId() {
    return externalRunId;
  }

  public String getPipelineName() {
    return pipelineName;
  }

  public int getRunAttempt() {
    return runAttempt;
  }

  public PipelineRunStatus getPipelineStatus() {
    return pipelineStatus;
  }

  public String getCommitSha() {
    return commitSha;
  }

  public String getGitRef() {
    return gitRef;
  }

  public String getEnvironmentName() {
    return environmentName;
  }

  public String getEvidenceSummary() {
    return evidenceSummary;
  }

  public List<String> getSourceFields() {
    return sourceFields;
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }

  private static int requirePositive(int value, String fieldName) {
    if (value <= 0) {
      throw new IllegalArgumentException(fieldName + " must be positive");
    }
    return value;
  }

  private static List<String> requireSourceFields(List<String> values) {
    if (values == null || values.isEmpty()) {
      throw new IllegalArgumentException("sourceFields must not be empty");
    }
    values.forEach(value -> requireText(value, "sourceField"));
    return List.copyOf(values);
  }
}
