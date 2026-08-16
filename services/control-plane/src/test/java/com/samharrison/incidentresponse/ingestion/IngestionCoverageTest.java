package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.coverage.ProductionSurfaceCoverageSupport;
import org.junit.jupiter.api.Test;

class IngestionCoverageTest {
  @Test
  void exercisesProductionSurface() throws Exception {
    ProductionSurfaceCoverageSupport.exercisePackage("com.samharrison.incidentresponse.ingestion");
  }
}
