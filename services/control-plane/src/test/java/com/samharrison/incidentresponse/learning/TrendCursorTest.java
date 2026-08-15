package com.samharrison.incidentresponse.learning;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TrendCursorTest {
  @Test
  void roundTripsDeterministically() {
    TrendCursor cursor =
        new TrendCursor(
            Instant.parse("2026-08-15T12:00:00Z"),
            UUID.fromString("00000000-0000-0000-0000-000000000012"));
    assertThat(TrendCursor.decode(cursor.encode())).isEqualTo(cursor);
    assertThat(cursor.encode()).isEqualTo(cursor.encode());
  }
}
