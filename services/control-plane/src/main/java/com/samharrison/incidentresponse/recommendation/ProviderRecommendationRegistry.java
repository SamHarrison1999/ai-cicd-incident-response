package com.samharrison.incidentresponse.recommendation;

import org.springframework.stereotype.Component;

@Component
public class ProviderRecommendationRegistry {
  private final RecommendationProvider provider;

  public ProviderRecommendationRegistry(DeterministicRecommendationProvider provider) {
    this.provider = provider;
  }

  public RecommendationProvider active() {
    return provider;
  }
}
