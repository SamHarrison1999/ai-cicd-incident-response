package com.samharrison.incidentresponse.project;

import com.samharrison.incidentresponse.coverage.ProductionSurfaceCoverageSupport;
import org.junit.jupiter.api.Test;

class ProjectCoverageTest {
  @Test
  void exercisesProductionSurface() throws Exception {
    ProductionSurfaceCoverageSupport.exercisePackage("com.samharrison.incidentresponse.project");
  }
}
