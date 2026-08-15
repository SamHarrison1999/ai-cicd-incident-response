package com.samharrison.incidentresponse.recommendation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ProviderRecommendationRequest(
    UUID organisationId,
    UUID projectId,
    UUID incidentId,
    List<EvidenceBundleAssembler.EvidenceSummary> evidence,
    List<EvidenceBundleAssembler.HistoricalSummary> historical,
    String rulesetVersion) {
  public ProviderRecommendationRequest {
    Objects.requireNonNull(organisationId);
    Objects.requireNonNull(projectId);
    evidence = List.copyOf(Objects.requireNonNull(evidence));
    historical = List.copyOf(Objects.requireNonNull(historical));
    rulesetVersion = required(rulesetVersion, 80);
  }

  private static String required(String value, int max) {
    if (value == null || value.isBlank() || value.length() > max) {
      throw new IllegalArgumentException("rulesetVersion is outside the permitted range");
    }
    return value;
  }
}
