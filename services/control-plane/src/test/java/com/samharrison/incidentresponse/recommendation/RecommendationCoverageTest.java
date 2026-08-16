package com.samharrison.incidentresponse.recommendation;

import com.samharrison.incidentresponse.coverage.ProductionSurfaceCoverageSupport;
import org.junit.jupiter.api.Test;

class RecommendationCoverageTest {
  @Test
  void exercisesProductionSurface() throws Exception {
    ProductionSurfaceCoverageSupport.exercisePackage(
        "com.samharrison.incidentresponse.recommendation");
  }
}
