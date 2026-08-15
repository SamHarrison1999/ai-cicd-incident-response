package com.samharrison.incidentresponse.review;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationReviewRepository extends JpaRepository<RecommendationReview, UUID> {
  List<RecommendationReview>
      findAllByOrganisationIdAndProjectIdAndRecommendationIdOrderByCreatedAtDescIdDesc(
          UUID organisationId, UUID projectId, UUID recommendationId);
}
