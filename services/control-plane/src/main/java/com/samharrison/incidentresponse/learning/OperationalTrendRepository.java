package com.samharrison.incidentresponse.learning;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationalTrendRepository extends JpaRepository<OperationalTrend, UUID> {
  List<OperationalTrend>
      findAllByOrganisationIdAndProjectIdOrderByWindowEndDescDimensionAscDimensionKeyAscIdDesc(
          UUID organisationId, UUID projectId);
}
