package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.ingestion.NormalisedEventType;
import com.samharrison.incidentresponse.ingestion.PipelineRunStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CorrelationEvent(
    UUID eventId,
    UUID organisationId,
    UUID projectId,
    Instant occurredAt,
    NormalisedEventType eventType,
    PipelineRunStatus pipelineStatus,
    String commitSha,
    String environmentName,
    String externalRunId,
    int attempt) {

  public CorrelationEvent {
    Objects.requireNonNull(eventId);
    Objects.requireNonNull(organisationId);
    Objects.requireNonNull(projectId);
    Objects.requireNonNull(occurredAt);
    Objects.requireNonNull(eventType);
    Objects.requireNonNull(pipelineStatus);
    if (externalRunId == null || externalRunId.isBlank()) {
      throw new IllegalArgumentException("externalRunId must not be blank");
    }
    if (attempt <= 0) {
      throw new IllegalArgumentException("attempt must be positive");
    }
  }
}
