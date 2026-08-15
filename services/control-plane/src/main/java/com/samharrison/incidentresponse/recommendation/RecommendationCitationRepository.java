package com.samharrison.incidentresponse.recommendation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationCitationRepository
    extends JpaRepository<RecommendationCitation, UUID> {
  List<RecommendationCitation> findAllByRecommendationIdOrderByIdAsc(UUID recommendationId);
}
