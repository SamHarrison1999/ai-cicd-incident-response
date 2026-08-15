package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RecommendationControllerContractTest {
  @Test
  void requestDefaultsToEmptyEvidenceLists() {
    RecommendationController.GenerateRequest request =
        new RecommendationController.GenerateRequest(null, null, null);
    assertThat(request.evidenceIds()).isEmpty();
    assertThat(request.historicalRecordIds()).isEmpty();
  }
}
