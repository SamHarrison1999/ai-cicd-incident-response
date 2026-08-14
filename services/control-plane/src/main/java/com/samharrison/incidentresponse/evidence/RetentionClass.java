package com.samharrison.incidentresponse.evidence;

import java.time.Duration;

public enum RetentionClass {
  SHORT(Duration.ofDays(7)),
  STANDARD(Duration.ofDays(30)),
  EXTENDED(Duration.ofDays(365));

  private final Duration duration;

  RetentionClass(Duration duration) {
    this.duration = duration;
  }

  public Duration duration() {
    return duration;
  }
}
