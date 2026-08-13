package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class IngestionMetricsTest {
  @Test
  void recordsNormalisedAndUnsupportedCounters() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    IngestionMetrics metrics = new IngestionMetrics(registry);

    metrics.recordNormalisedEvent();
    metrics.recordUnsupportedEvent();
    metrics.recordUnsupportedEvent();

    assertThat(registry.get("cicd.ingestion.normalised.events").counter().count()).isEqualTo(1.0);
    assertThat(registry.get("cicd.ingestion.unsupported.events").counter().count()).isEqualTo(2.0);
  }
}
