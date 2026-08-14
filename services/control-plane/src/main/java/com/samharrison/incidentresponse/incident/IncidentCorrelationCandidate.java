package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.ingestion.NormalisedEventType;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record IncidentCorrelationCandidate(
    UUID incidentId,
    UUID organisationId,
    UUID projectId,
    IncidentStatus status,
    Instant detectedAt,
    NormalisedEventType eventType,
    String commitSha,
    String environmentName,
    String externalRunId,
    int attempt) {

  public IncidentCorrelationCandidate {
    Objects.requireNonNull(incidentId);
    Objects.requireNonNull(organisationId);
    Objects.requireNonNull(projectId);
    Objects.requireNonNull(status);
    Objects.requireNonNull(detectedAt);
    Objects.requireNonNull(eventType);
    if (externalRunId == null || externalRunId.isBlank()) {
      throw new IllegalArgumentException("externalRunId must not be blank");
    }
    if (attempt <= 0) {
      throw new IllegalArgumentException("attempt must be positive");
    }
  }
}
