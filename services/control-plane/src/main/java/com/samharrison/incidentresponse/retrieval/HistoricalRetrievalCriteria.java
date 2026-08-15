package com.samharrison.incidentresponse.retrieval;

import java.time.Instant;

public record HistoricalRetrievalCriteria(
    String diagnosisCategory,
    String provider,
    String pipelineName,
    String environmentName,
    String gitRef,
    String commitSha,
    Instant occurredFrom,
    Instant occurredTo,
    String query,
    int limit) {

  public HistoricalRetrievalCriteria {
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("limit must be between 1 and 100");
    }
    if (occurredFrom != null && occurredTo != null && occurredFrom.isAfter(occurredTo)) {
      throw new IllegalArgumentException("occurredFrom must not be after occurredTo");
    }
  }
}
