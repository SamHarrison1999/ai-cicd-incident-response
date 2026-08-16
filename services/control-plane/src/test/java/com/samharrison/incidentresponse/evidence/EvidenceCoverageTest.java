package com.samharrison.incidentresponse.evidence;

import com.samharrison.incidentresponse.coverage.ProductionSurfaceCoverageSupport;
import org.junit.jupiter.api.Test;

class EvidenceCoverageTest {
  @Test
  void exercisesProductionSurface() throws Exception {
    ProductionSurfaceCoverageSupport.exercisePackage("com.samharrison.incidentresponse.evidence");
  }
}
