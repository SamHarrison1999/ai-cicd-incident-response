package com.samharrison.incidentresponse.diagnosis;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record DiagnosisResult(
    String ruleVersion,
    DiagnosisCategory category,
    double confidence,
    List<UUID> supportingSignalIds,
    List<String> missingEvidence,
    List<String> warnings,
    String abstentionReason) {

  public DiagnosisResult {
    Objects.requireNonNull(ruleVersion);
    Objects.requireNonNull(category);
    if (confidence < 0.0 || confidence > 1.0 || Double.isNaN(confidence)) {
      throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
    }
    supportingSignalIds = List.copyOf(Objects.requireNonNull(supportingSignalIds));
    missingEvidence = List.copyOf(Objects.requireNonNull(missingEvidence));
    warnings = List.copyOf(Objects.requireNonNull(warnings));
    if (category == DiagnosisCategory.INSUFFICIENT_EVIDENCE
        || category == DiagnosisCategory.UNKNOWN) {
      if (abstentionReason == null || abstentionReason.isBlank()) {
        throw new IllegalArgumentException("abstentionReason is required for an abstained result");
      }
    } else if (abstentionReason != null && !abstentionReason.isBlank()) {
      throw new IllegalArgumentException("suspected results must not contain an abstention reason");
    }
  }
}
