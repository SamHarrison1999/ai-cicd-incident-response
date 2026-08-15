package com.samharrison.incidentresponse.learning;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DeterministicTrendObservationService {
  private static final int MINIMUM_SAMPLE_SIZE = 3;

  public OperationalTrend observe(
      UUID organisationId,
      UUID projectId,
      TrendDimension dimension,
      String dimensionKey,
      ObservationWindow window,
      String aggregationVersion,
      List<TrendObservation> observations) {
    Objects.requireNonNull(organisationId);
    Objects.requireNonNull(projectId);
    Objects.requireNonNull(dimension);
    Objects.requireNonNull(dimensionKey);
    Objects.requireNonNull(window);
    Objects.requireNonNull(aggregationVersion);
    List<TrendObservation> matching =
        observations.stream()
            .filter(item -> organisationId.equals(item.organisationId()))
            .filter(item -> projectId.equals(item.projectId()))
            .filter(item -> dimension == item.dimension())
            .filter(item -> dimensionKey.equals(item.dimensionKey()))
            .filter(item -> !item.observedAt().isBefore(window.start()))
            .filter(item -> !item.observedAt().isAfter(window.end()))
            .sorted(
                java.util.Comparator.comparing(TrendObservation::observedAt)
                    .thenComparing(TrendObservation::sourceReference))
            .toList();
    TrendSuppressionReason suppressionReason =
        matching.size() < MINIMUM_SAMPLE_SIZE
            ? TrendSuppressionReason.INSUFFICIENT_SAMPLE
            : TrendSuppressionReason.NONE;
    return new OperationalTrend(
        UUID.randomUUID(),
        organisationId,
        projectId,
        dimension,
        dimensionKey,
        window,
        aggregationVersion,
        matching.size(),
        matching.size(),
        matching.isEmpty() ? "none" : matching.get(0).sourceReference(),
        suppressionReason);
  }
}
