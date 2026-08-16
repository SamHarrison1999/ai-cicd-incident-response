package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProviderRecommendationCandidateCoverageTest {
  @Test
  void enforcesConfidenceAndAbstentionBounds() {
    assertThatThrownBy(() -> candidate(-0.1, RecommendationStatus.RECOMMENDED, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate(1.1, RecommendationStatus.RECOMMENDED, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate(Double.NaN, RecommendationStatus.RECOMMENDED, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate(0.0, RecommendationStatus.ABSTAINED, " "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static ProviderRecommendationCandidate candidate(
      double confidence, RecommendationStatus status, String reason) {
    return new ProviderRecommendationCandidate(
        "dependency",
        "summary",
        "cause",
        confidence,
        "explanation",
        status,
        reason,
        "deterministic",
        "v1");
  }
}
