package com.samharrison.incidentresponse.learning;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeterministicTrendObservationServiceTest {
  @Test
  void filtersByTenantDimensionKeyAndWindowBeforeAggregation() {
    UUID organisation = UUID.randomUUID();
    UUID project = UUID.randomUUID();
    Instant start = Instant.parse("2026-08-15T00:00:00Z");
    Instant end = Instant.parse("2026-08-15T23:59:59Z");
    OperationalTrend trend =
        new DeterministicTrendObservationService()
            .observe(
                organisation,
                project,
                TrendDimension.INCIDENT_CATEGORY,
                "dependency",
                new ObservationWindow(start, end),
                "trend-v1",
                List.of(
                    observation(organisation, project, "dependency", "2026-08-15T01:00:00Z"),
                    observation(organisation, project, "dependency", "2026-08-15T02:00:00Z"),
                    observation(organisation, project, "dependency", "2026-08-15T03:00:00Z"),
                    observation(organisation, project, "dependency", "2026-08-14T23:59:59Z"),
                    observation(organisation, project, "dependency", "2026-08-16T00:00:00Z"),
                    observation(UUID.randomUUID(), project, "dependency", "2026-08-15T04:00:00Z"),
                    observation(organisation, project, "other", "2026-08-15T05:00:00Z")));
    assertThat(trend.getSampleCount()).isEqualTo(3);
    assertThat(trend.getSuppressionReason()).isEqualTo(TrendSuppressionReason.NONE);
    assertThat(trend.getAggregationVersion()).isEqualTo("trend-v1");
  }

  @Test
  void suppressesInsufficientSamples() {
    UUID organisation = UUID.randomUUID();
    UUID project = UUID.randomUUID();
    Instant now = Instant.parse("2026-08-15T12:00:00Z");
    OperationalTrend trend =
        new DeterministicTrendObservationService()
            .observe(
                organisation,
                project,
                TrendDimension.REVIEW_EFFORT,
                "edited",
                new ObservationWindow(now.minusSeconds(60), now),
                "trend-v1",
                List.of(observation(organisation, project, "edited", now.toString())));
    assertThat(trend.getSuppressionReason()).isEqualTo(TrendSuppressionReason.INSUFFICIENT_SAMPLE);
  }

  private static TrendObservation observation(
      UUID organisation, UUID project, String key, String observedAt) {
    return new TrendObservation(
        organisation,
        project,
        TrendDimension.INCIDENT_CATEGORY,
        key,
        Instant.parse(observedAt),
        "feedback:source-1");
  }
}
