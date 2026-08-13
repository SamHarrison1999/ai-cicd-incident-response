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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "pipeline_runs",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_pipeline_run_source_external_attempt",
            columnNames = {"event_source_id", "external_run_id", "attempt"}))
public class PipelineRun {

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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private EventProvider provider;

  @Column(name = "external_run_id", nullable = false, length = 200)
  private String externalRunId;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(nullable = false)
  private int attempt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private PipelineRunStatus status;

  @Column(name = "commit_sha", length = 64)
  private String commitSha;

  @Column(name = "git_ref", length = 500)
  private String gitRef;

  @Column(name = "environment_name", length = 120)
  private String environmentName;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "last_event_occurred_at", nullable = false)
  private Instant lastEventOccurredAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected PipelineRun() {}

  public PipelineRun(
      UUID id,
      Organisation organisation,
      Project project,
      EventSource eventSource,
      EventProvider provider,
      String externalRunId,
      String name,
      int attempt,
      PipelineRunStatus status,
      String commitSha,
      String gitRef,
      String environmentName,
      Instant lastEventOccurredAt) {
    this.id = Objects.requireNonNull(id);
    this.organisation = Objects.requireNonNull(organisation);
    this.project = Objects.requireNonNull(project);
    this.eventSource = Objects.requireNonNull(eventSource);
    this.provider = Objects.requireNonNull(provider);
    this.externalRunId = requireText(externalRunId, "externalRunId");
    this.name = requireText(name, "name");
    this.attempt = requirePositive(attempt, "attempt");
    this.status = Objects.requireNonNull(status);
    this.commitSha = commitSha;
    this.gitRef = gitRef;
    this.environmentName = environmentName;
    this.lastEventOccurredAt = Objects.requireNonNull(lastEventOccurredAt);
    if (status == PipelineRunStatus.RUNNING) {
      startedAt = lastEventOccurredAt;
    }
    if (isTerminal(status)) {
      completedAt = lastEventOccurredAt;
    }
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
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

  public EventProvider getProvider() {
    return provider;
  }

  public String getExternalRunId() {
    return externalRunId;
  }

  public String getName() {
    return name;
  }

  public int getAttempt() {
    return attempt;
  }

  public PipelineRunStatus getStatus() {
    return status;
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

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public Instant getLastEventOccurredAt() {
    return lastEventOccurredAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public long getVersion() {
    return version;
  }

  public void applyStatus(PipelineRunStatus nextStatus, Instant occurredAt) {
    Objects.requireNonNull(nextStatus);
    Objects.requireNonNull(occurredAt);
    if (occurredAt.isBefore(lastEventOccurredAt)) {
      throw new IllegalArgumentException(
          "pipeline status evidence is older than the current projection");
    }
    if (isTerminal(status) && !isTerminal(nextStatus)) {
      throw new IllegalStateException(
          "terminal pipeline runs cannot regress to a non-terminal status");
    }
    status = nextStatus;
    lastEventOccurredAt = occurredAt;
    if (nextStatus == PipelineRunStatus.RUNNING && startedAt == null) {
      startedAt = occurredAt;
    }
    if (isTerminal(nextStatus)) {
      completedAt = occurredAt;
    }
  }

  private static boolean isTerminal(PipelineRunStatus value) {
    return switch (value) {
      case SUCCEEDED, FAILED, CANCELLED, SKIPPED, TIMED_OUT -> true;
      default -> false;
    };
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
}
