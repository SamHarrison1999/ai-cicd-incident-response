package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.coverage.ProductionSurfaceCoverageSupport;
import org.junit.jupiter.api.Test;

class IncidentCoverageTest {
  @Test
  void exercisesProductionSurface() throws Exception {
    ProductionSurfaceCoverageSupport.exercisePackage("com.samharrison.incidentresponse.incident");
  }
}
