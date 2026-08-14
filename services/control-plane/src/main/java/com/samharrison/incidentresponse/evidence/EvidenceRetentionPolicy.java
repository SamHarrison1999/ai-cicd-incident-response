package com.samharrison.incidentresponse.evidence;

import java.time.Instant;
import java.util.Objects;

public final class EvidenceRetentionPolicy {

  public boolean isExpired(RetentionClass retentionClass, Instant ingestedAt, Instant now) {
    Objects.requireNonNull(retentionClass);
    Objects.requireNonNull(ingestedAt);
    Objects.requireNonNull(now);
    return !now.isBefore(ingestedAt.plus(retentionClass.duration()));
  }
}
