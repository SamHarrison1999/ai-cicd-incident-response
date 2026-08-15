package com.samharrison.incidentresponse.retrieval;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HistoricalRetrievalSecurityContractTest {

  @Test
  void rejectsOversizedPages() {
    assertThatThrownBy(
            () ->
                new HistoricalRetrievalCriteria(
                    null, null, null, null, null, null, null, null, null, 101))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("limit");
  }

  @Test
  void rejectsReversedTimeRanges() {
    assertThatThrownBy(
            () ->
                new HistoricalRetrievalCriteria(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    java.time.Instant.parse("2026-08-15T00:00:00Z"),
                    java.time.Instant.parse("2026-08-14T00:00:00Z"),
                    null,
                    25))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("occurredFrom");
  }
}
