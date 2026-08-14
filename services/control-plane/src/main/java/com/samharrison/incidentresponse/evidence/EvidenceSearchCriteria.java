package com.samharrison.incidentresponse.evidence;

import java.time.Instant;

public record EvidenceSearchCriteria(
    EvidenceKind kind,
    String sourceSystem,
    String query,
    Instant occurredFrom,
    Instant occurredTo,
    int limit) {

  public EvidenceSearchCriteria {
    sourceSystem = normalise(sourceSystem);
    query = normalise(query);
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("limit must be between 1 and 100");
    }
    if (occurredFrom != null && occurredTo != null && occurredFrom.isAfter(occurredTo)) {
      throw new IllegalArgumentException("occurredFrom must not be after occurredTo");
    }
  }

  private static String normalise(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
