package com.samharrison.incidentresponse.learning;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TrendObservation(
    UUID organisationId,
    UUID projectId,
    TrendDimension dimension,
    String dimensionKey,
    Instant observedAt,
    String sourceReference) {
  public TrendObservation {
    Objects.requireNonNull(organisationId);
    Objects.requireNonNull(projectId);
    Objects.requireNonNull(dimension);
    Objects.requireNonNull(observedAt);
    if (dimensionKey == null || dimensionKey.isBlank() || dimensionKey.length() > 96) {
      throw new IllegalArgumentException("dimensionKey is outside the permitted range");
    }
    if (sourceReference == null || sourceReference.isBlank() || sourceReference.length() > 128) {
      throw new IllegalArgumentException("sourceReference is outside the permitted range");
    }
  }
}
