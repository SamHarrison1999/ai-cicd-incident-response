package com.samharrison.incidentresponse.learning;

import java.time.Instant;
import java.util.Objects;

public record ObservationWindow(Instant start, Instant end) {
  public ObservationWindow {
    Objects.requireNonNull(start);
    Objects.requireNonNull(end);
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("observation window is invalid");
    }
  }
}
