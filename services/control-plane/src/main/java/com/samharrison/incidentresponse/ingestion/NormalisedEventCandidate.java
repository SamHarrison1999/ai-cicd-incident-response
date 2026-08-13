package com.samharrison.incidentresponse.ingestion;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

record NormalisedEventCandidate(
    NormalisedEventType eventType,
    Instant occurredAt,
    String externalRunId,
    String pipelineName,
    int runAttempt,
    PipelineRunStatus pipelineStatus,
    String commitSha,
    String gitRef,
    String environmentName,
    String evidenceSummary,
    List<String> sourceFields) {

  NormalisedEventCandidate {
    Objects.requireNonNull(eventType);
    Objects.requireNonNull(occurredAt);
    Objects.requireNonNull(pipelineStatus);
    if (externalRunId == null || externalRunId.isBlank()) {
      throw new IllegalArgumentException("externalRunId must not be blank");
    }
    if (pipelineName == null || pipelineName.isBlank()) {
      throw new IllegalArgumentException("pipelineName must not be blank");
    }
    if (runAttempt < 1) {
      throw new IllegalArgumentException("runAttempt must be positive");
    }
    if (evidenceSummary == null || evidenceSummary.isBlank()) {
      throw new IllegalArgumentException("evidenceSummary must not be blank");
    }
    if (sourceFields == null || sourceFields.isEmpty()) {
      throw new IllegalArgumentException("sourceFields must not be empty");
    }
    sourceFields = List.copyOf(sourceFields);
  }
}
