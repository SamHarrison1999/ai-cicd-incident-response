package com.samharrison.incidentresponse.ingestion;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class IngestionMetrics {
  private final Counter normalisedEvents;
  private final Counter unsupportedEvents;

  public IngestionMetrics(MeterRegistry meterRegistry) {
    normalisedEvents =
        Counter.builder("cicd.ingestion.normalised.events")
            .description("Normalised CI/CD events created from verified webhook deliveries")
            .register(meterRegistry);
    unsupportedEvents =
        Counter.builder("cicd.ingestion.unsupported.events")
            .description("Verified webhook deliveries with no supported provider mapping")
            .register(meterRegistry);
  }

  public void recordNormalisedEvent() {
    normalisedEvents.increment();
  }

  public void recordUnsupportedEvent() {
    unsupportedEvents.increment();
  }
}
