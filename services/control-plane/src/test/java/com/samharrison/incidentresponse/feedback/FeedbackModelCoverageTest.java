package com.samharrison.incidentresponse.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FeedbackModelCoverageTest {

  @Test
  void criteriaDefaultsAndAggregateExposeBoundedFields() {
    FeedbackQueryCriteria defaults = FeedbackQueryCriteria.defaults();
    assertThat(defaults.limit()).isEqualTo(50);
    UUID id = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Instant start = Instant.parse("2026-08-15T00:00:00Z");
    Instant end = Instant.parse("2026-08-16T00:00:00Z");
    FeedbackAggregate aggregate =
        new FeedbackAggregate(
            id,
            organisationId,
            projectId,
            "v1",
            start,
            end,
            4,
            1,
            1,
            1,
            1,
            FeedbackSuppressionReason.NONE);

    assertThat(aggregate.getId()).isEqualTo(id);
    assertThat(aggregate.getOrganisationId()).isEqualTo(organisationId);
    assertThat(aggregate.getProjectId()).isEqualTo(projectId);
    assertThat(aggregate.getPolicyVersion()).isEqualTo("v1");
    assertThat(aggregate.getWindowStart()).isEqualTo(start);
    assertThat(aggregate.getWindowEnd()).isEqualTo(end);
    assertThat(aggregate.getSampleCount()).isEqualTo(4);
    assertThat(aggregate.getAcceptedCount()).isEqualTo(1);
    assertThat(aggregate.getEditedCount()).isEqualTo(1);
    assertThat(aggregate.getRejectedCount()).isEqualTo(1);
    assertThat(aggregate.getResolvedCount()).isEqualTo(1);
    assertThat(aggregate.getSuppressionReason()).isEqualTo(FeedbackSuppressionReason.NONE);
  }

  @Test
  void rejectsInvalidCriteriaAndAggregateBounds() {
    assertThatThrownBy(() -> new FeedbackQueryCriteria(null, null, null, 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new FeedbackQueryCriteria(null, null, null, 51))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new FeedbackQueryCriteria(
                    null,
                    Instant.parse("2026-08-16T00:00:00Z"),
                    Instant.parse("2026-08-15T00:00:00Z"),
                    1))
        .isInstanceOf(IllegalArgumentException.class);
    new FeedbackQueryCriteria(null, Instant.EPOCH, null, 1);
    new FeedbackQueryCriteria(null, null, Instant.EPOCH, 1);
    assertThatThrownBy(() -> new FeedbackQueryCriteria(" ", null, null, 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new FeedbackQueryCriteria("x".repeat(65), null, null, 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new FeedbackAggregate(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "v1",
                    Instant.EPOCH,
                    Instant.EPOCH.minusSeconds(1),
                    1,
                    0,
                    0,
                    0,
                    0,
                    FeedbackSuppressionReason.NONE))
        .isInstanceOf(IllegalArgumentException.class);
    for (int[] counts :
        new int[][] {
          {-1, 0, 0, 0, 0}, {1, -1, 0, 0, 0}, {1, 0, -1, 0, 0}, {1, 0, 0, -1, 0}, {1, 0, 0, 0, -1}
        }) {
      assertThatThrownBy(
              () ->
                  new FeedbackAggregate(
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      UUID.randomUUID(),
                      "v1",
                      Instant.EPOCH,
                      Instant.EPOCH,
                      counts[0],
                      counts[1],
                      counts[2],
                      counts[3],
                      counts[4],
                      FeedbackSuppressionReason.NONE))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
