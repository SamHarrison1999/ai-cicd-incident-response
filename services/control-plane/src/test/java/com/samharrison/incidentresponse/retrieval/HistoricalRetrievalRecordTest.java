package com.samharrison.incidentresponse.retrieval;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HistoricalRetrievalRecordTest {

  @Test
  void rejectsUnboundedSummary() {
    String summary = "x".repeat(2001);

    assertThatThrownBy(
            () ->
                new HistoricalRetrievalRecord(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    HistoricalSourceKind.EVIDENCE,
                    UUID.randomUUID(),
                    Instant.parse("2026-08-14T10:00:00Z"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    summary,
                    "metadata match",
                    "evidence:1",
                    Instant.parse("2026-08-14T10:01:00Z")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("summary");
  }
}
