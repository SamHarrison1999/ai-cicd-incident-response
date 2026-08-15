package com.samharrison.incidentresponse.review;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewedRecommendationVersionRepository
    extends JpaRepository<ReviewedRecommendationVersion, UUID> {
  Optional<ReviewedRecommendationVersion>
      findTopByOrganisationIdAndProjectIdAndRecommendationIdOrderByVersionNumberDesc(
          UUID organisationId, UUID projectId, UUID recommendationId);
}
