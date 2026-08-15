package com.samharrison.incidentresponse.recommendation;

public record ProviderRecommendationCandidate(
    String category,
    String summary,
    String likelyCause,
    double confidence,
    String confidenceExplanation,
    RecommendationStatus status,
    String abstentionReason,
    String providerName,
    String modelVersion) {
  public ProviderRecommendationCandidate {
    if (confidence < 0 || confidence > 1 || Double.isNaN(confidence)) {
      throw new IllegalArgumentException("confidence must be between zero and one");
    }
    if (status == RecommendationStatus.ABSTAINED
        && (abstentionReason == null || abstentionReason.isBlank())) {
      throw new IllegalArgumentException("abstentionReason is required for abstained candidates");
    }
  }
}
