package com.samharrison.incidentresponse.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HistoricalRetrievalCursorTest {

  @Test
  void roundTripsStableOrderingKey() {
    HistoricalRetrievalCursor cursor =
        new HistoricalRetrievalCursor(
            Instant.parse("2026-08-14T12:00:00Z"),
            UUID.fromString("00000000-0000-0000-0000-000000000009"));

    assertThat(HistoricalRetrievalCursor.decode(cursor.encode())).isEqualTo(cursor);
  }

  @Test
  void rejectsMalformedCursor() {
    assertThatThrownBy(() -> HistoricalRetrievalCursor.decode("not-a-cursor"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
