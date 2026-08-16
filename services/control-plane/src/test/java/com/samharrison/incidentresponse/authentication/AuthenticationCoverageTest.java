package com.samharrison.incidentresponse.authentication;

import com.samharrison.incidentresponse.coverage.ProductionSurfaceCoverageSupport;
import org.junit.jupiter.api.Test;

class AuthenticationCoverageTest {
  @Test
  void exercisesProductionSurface() throws Exception {
    ProductionSurfaceCoverageSupport.exercisePackage(
        "com.samharrison.incidentresponse.authentication");
  }
}
