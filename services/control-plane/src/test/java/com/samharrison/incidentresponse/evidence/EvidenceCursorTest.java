package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceCursorTest {

  @Test
  void roundTripPreservesTheOrderingPosition() {
    EvidenceCursor cursor =
        new EvidenceCursor(
            Instant.parse("2026-08-14T13:00:00Z"),
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));

    assertThat(EvidenceCursor.decode(cursor.encode())).isEqualTo(cursor);
  }

  @Test
  void blankCursorMeansTheFirstPage() {
    assertThat(EvidenceCursor.decode(null)).isNull();
    assertThat(EvidenceCursor.decode(" ")).isNull();
  }
}
