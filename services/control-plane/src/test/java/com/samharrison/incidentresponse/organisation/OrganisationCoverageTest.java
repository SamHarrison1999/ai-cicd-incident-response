package com.samharrison.incidentresponse.organisation;

import com.samharrison.incidentresponse.coverage.ProductionSurfaceCoverageSupport;
import org.junit.jupiter.api.Test;

class OrganisationCoverageTest {
  @Test
  void exercisesProductionSurface() throws Exception {
    ProductionSurfaceCoverageSupport.exercisePackage(
        "com.samharrison.incidentresponse.organisation");
  }
}
