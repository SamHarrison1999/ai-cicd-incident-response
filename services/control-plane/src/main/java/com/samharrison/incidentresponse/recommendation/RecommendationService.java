package com.samharrison.incidentresponse.recommendation;

import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

  private final RecommendationRepository recommendationRepository;
  private final TenantAccessService tenantAccessService;

  public RecommendationService(
      RecommendationRepository recommendationRepository, TenantAccessService tenantAccessService) {
    this.recommendationRepository = recommendationRepository;
    this.tenantAccessService = tenantAccessService;
  }

  @Transactional(readOnly = true)
  public List<Recommendation> list(UUID userId, UUID organisationId, UUID projectId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return recommendationRepository.findAllByOrganisationIdAndProjectIdOrderByGeneratedAtDescIdDesc(
        organisationId, projectId);
  }

  @Transactional
  public Recommendation save(UUID userId, Recommendation recommendation) {
    tenantAccessService.requireActiveMembership(recommendation.getOrganisationId(), userId);
    return recommendationRepository.save(recommendation);
  }
}
