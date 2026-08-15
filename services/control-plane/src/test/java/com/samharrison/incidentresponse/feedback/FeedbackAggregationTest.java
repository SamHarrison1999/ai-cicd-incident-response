package com.samharrison.incidentresponse.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FeedbackAggregationTest {
  @Test
  void aggregatesOnlyMatchingTenantPolicyAndWindow() {
    UUID organisation = UUID.randomUUID();
    UUID project = UUID.randomUUID();
    Instant start = Instant.parse("2026-08-15T00:00:00Z");
    Instant end = Instant.parse("2026-08-15T23:59:59Z");
    List<FeedbackSignal> signals =
        List.of(
            signal(
                organisation,
                project,
                FeedbackOutcome.ACCEPTED,
                "v1",
                Instant.parse("2026-08-15T01:00:00Z")),
            signal(
                organisation,
                project,
                FeedbackOutcome.EDITED,
                "v1",
                Instant.parse("2026-08-15T02:00:00Z")),
            signal(
                organisation,
                project,
                FeedbackOutcome.REJECTED,
                "v1",
                Instant.parse("2026-08-15T03:00:00Z")),
            signal(
                UUID.randomUUID(),
                project,
                FeedbackOutcome.RESOLVED,
                "v1",
                Instant.parse("2026-08-15T04:00:00Z")));
    FeedbackAggregate aggregate =
        new DeterministicFeedbackAggregationService()
            .aggregate(organisation, project, "v1", start, end, signals);
    assertThat(aggregate.getSampleCount()).isEqualTo(3);
    assertThat(aggregate.getAcceptedCount()).isEqualTo(1);
    assertThat(aggregate.getEditedCount()).isEqualTo(1);
    assertThat(aggregate.getRejectedCount()).isEqualTo(1);
    assertThat(aggregate.getSuppressionReason()).isEqualTo(FeedbackSuppressionReason.NONE);
  }

  @Test
  void suppressesSmallSamples() {
    UUID organisation = UUID.randomUUID();
    UUID project = UUID.randomUUID();
    Instant now = Instant.parse("2026-08-15T01:00:00Z");
    FeedbackAggregate aggregate =
        new DeterministicFeedbackAggregationService()
            .aggregate(
                organisation,
                project,
                "v1",
                now.minusSeconds(10),
                now,
                List.of(signal(organisation, project, FeedbackOutcome.ACCEPTED, "v1", now)));
    assertThat(aggregate.getSuppressionReason())
        .isEqualTo(FeedbackSuppressionReason.INSUFFICIENT_SAMPLE);
  }

  private static FeedbackSignal signal(
      UUID organisation, UUID project, FeedbackOutcome outcome, String policy, Instant time) {
    return new FeedbackSignal(
        UUID.randomUUID(),
        organisation,
        project,
        UUID.randomUUID(),
        UUID.randomUUID(),
        outcome,
        policy,
        time);
  }
}
