package com.samharrison.incidentresponse.evidence;

import java.util.List;
import java.util.Objects;

public record SanitisedEvidence(
    String sanitiserVersion, String content, int lineCount, List<SanitisationWarning> warnings) {

  public SanitisedEvidence {
    Objects.requireNonNull(sanitiserVersion);
    Objects.requireNonNull(content);
    if (content.isBlank()) {
      throw new IllegalArgumentException("content must not be blank");
    }
    if (lineCount <= 0) {
      throw new IllegalArgumentException("lineCount must be positive");
    }
    warnings = List.copyOf(Objects.requireNonNull(warnings));
  }
}
