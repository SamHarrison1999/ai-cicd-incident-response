package com.samharrison.incidentresponse.recommendation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
  List<Recommendation> findAllByOrganisationIdAndProjectIdOrderByGeneratedAtDescIdDesc(
      UUID organisationId, UUID projectId);
}
