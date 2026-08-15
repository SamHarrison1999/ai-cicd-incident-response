package com.samharrison.incidentresponse.learning;

import java.time.Instant;

public record TrendQueryCriteria(
    TrendDimension dimension, String dimensionKey, Instant from, Instant to, int limit) {
  public TrendQueryCriteria {
    if (limit < 1 || limit > 50) {
      throw new IllegalArgumentException("limit must be between 1 and 50");
    }
    if (from != null && to != null && to.isBefore(from)) {
      throw new IllegalArgumentException("trend query window is invalid");
    }
    if (dimensionKey != null && (dimensionKey.isBlank() || dimensionKey.length() > 96)) {
      throw new IllegalArgumentException("dimensionKey is outside the permitted range");
    }
  }

  public static TrendQueryCriteria defaults() {
    return new TrendQueryCriteria(null, null, null, null, 50);
  }
}
