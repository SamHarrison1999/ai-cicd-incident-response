package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TimelineCursorTest {

  @Test
  void roundTripPreservesTheCompleteOrderingPosition() {
    TimelineCursor cursor =
        new TimelineCursor(
            Instant.parse("2026-08-14T10:00:00Z"),
            Instant.parse("2026-08-14T10:00:01Z"),
            UUID.fromString("00000000-0000-0000-0000-000000000042"));

    assertThat(TimelineCursor.decode(cursor.encode())).isEqualTo(cursor);
  }

  @Test
  void malformedCursorIsRejected() {
    assertThatThrownBy(() -> TimelineCursor.decode("not-a-cursor"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("cursor is invalid");
  }
}
