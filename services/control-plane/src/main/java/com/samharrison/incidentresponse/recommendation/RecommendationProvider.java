package com.samharrison.incidentresponse.recommendation;

public interface RecommendationProvider {
  ProviderRecommendationCandidate generate(ProviderRecommendationRequest request);
}
