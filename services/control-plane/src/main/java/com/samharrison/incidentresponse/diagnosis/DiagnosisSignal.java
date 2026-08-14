package com.samharrison.incidentresponse.diagnosis;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DiagnosisSignal(
    UUID id, UUID evidenceId, String sourceSystem, Instant occurredAt, String sanitisedText) {

  public DiagnosisSignal {
    Objects.requireNonNull(id);
    Objects.requireNonNull(evidenceId);
    if (sourceSystem == null || sourceSystem.isBlank()) {
      throw new IllegalArgumentException("sourceSystem must not be blank");
    }
    Objects.requireNonNull(occurredAt);
    if (sanitisedText == null || sanitisedText.isBlank()) {
      throw new IllegalArgumentException("sanitisedText must not be blank");
    }
    if (sanitisedText.length() > 4000) {
      throw new IllegalArgumentException("sanitisedText exceeds the diagnosis bound");
    }
  }
}
