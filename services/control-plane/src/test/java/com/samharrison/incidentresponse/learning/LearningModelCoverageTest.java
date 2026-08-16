package com.samharrison.incidentresponse.learning;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LearningModelCoverageTest {
  @Test
  void rejectsInvalidWindowsCriteriaObservationsAndTrends() {
    Instant now = Instant.now();
    assertThatThrownBy(() -> new ObservationWindow(now, now.minusSeconds(1)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new TrendQueryCriteria(null, " ", null, null, 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new TrendQueryCriteria(null, "x".repeat(97), null, null, 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new TrendQueryCriteria(null, null, now, now.minusSeconds(1), 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new TrendObservation(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "",
                    now,
                    "source"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new TrendObservation(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "key",
                    now,
                    ""))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new OperationalTrend(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "key",
                    new ObservationWindow(now, now),
                    "v1",
                    1,
                    2,
                    "source",
                    TrendSuppressionReason.NONE))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new TrendQueryCriteria(null, null, null, null, 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new TrendQueryCriteria(null, null, null, null, 51))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new TrendObservation(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    null,
                    now,
                    "source"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new TrendObservation(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "x".repeat(97),
                    now,
                    "source"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new TrendObservation(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "key",
                    now,
                    null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new TrendObservation(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "key",
                    now,
                    "x".repeat(129)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new OperationalTrend(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "key",
                    new ObservationWindow(now, now),
                    "v1",
                    -1,
                    0,
                    "source",
                    TrendSuppressionReason.NONE))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new OperationalTrend(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "key",
                    new ObservationWindow(now, now),
                    "v1",
                    1,
                    -1,
                    "source",
                    TrendSuppressionReason.NONE))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
