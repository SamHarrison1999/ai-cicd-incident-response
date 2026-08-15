package com.samharrison.incidentresponse.review;

import com.samharrison.incidentresponse.recommendation.RecommendationRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewApiService {
  private final RecommendationRepository recommendationRepository;
  private final RecommendationReviewRepository reviewRepository;
  private final ReviewedRecommendationVersionRepository versionRepository;
  private final IncidentResolutionRepository resolutionRepository;
  private final TenantAccessService tenantAccessService;

  public ReviewApiService(
      RecommendationRepository recommendationRepository,
      RecommendationReviewRepository reviewRepository,
      ReviewedRecommendationVersionRepository versionRepository,
      IncidentResolutionRepository resolutionRepository,
      TenantAccessService tenantAccessService) {
    this.recommendationRepository = recommendationRepository;
    this.reviewRepository = reviewRepository;
    this.versionRepository = versionRepository;
    this.resolutionRepository = resolutionRepository;
    this.tenantAccessService = tenantAccessService;
  }

  public List<RecommendationReview> history(
      UUID userId, UUID organisationId, UUID projectId, UUID recommendationId) {
    requireRecommendation(userId, organisationId, projectId, recommendationId);
    return reviewRepository
        .findAllByOrganisationIdAndProjectIdAndRecommendationIdOrderByCreatedAtDescIdDesc(
            organisationId, projectId, recommendationId);
  }

  @Transactional
  public RecommendationReview submit(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      UUID recommendationId,
      ReviewAction action,
      ReviewReason reason,
      String comment,
      String editedSummary,
      String editedCause) {
    requireRecommendation(userId, organisationId, projectId, recommendationId);
    return new RecommendationReviewService(
            recommendationRepository, reviewRepository, versionRepository, tenantAccessService)
        .review(
            userId,
            organisationId,
            projectId,
            recommendationId,
            action,
            reason,
            comment,
            editedSummary,
            editedCause);
  }

  @Transactional
  public IncidentResolution resolve(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      UUID incidentId,
      UUID recommendationId,
      UUID reviewedVersionId,
      String resolutionText) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    ReviewedRecommendationVersion version =
        versionRepository
            .findById(reviewedVersionId)
            .filter(
                item ->
                    organisationId.equals(item.getOrganisationId())
                        && projectId.equals(item.getProjectId())
                        && recommendationId.equals(item.getRecommendationId()))
            .orElseThrow(
                () ->
                    new ReviewAccessException(HttpStatus.NOT_FOUND, "REVIEWED_VERSION_NOT_FOUND"));
    if (!recommendationRepository.existsById(recommendationId)) {
      throw new ReviewAccessException(HttpStatus.NOT_FOUND, "RECOMMENDATION_NOT_FOUND");
    }
    return resolutionRepository.save(
        new IncidentResolution(
            UUID.randomUUID(),
            organisationId,
            projectId,
            incidentId,
            recommendationId,
            version.getId(),
            resolutionText,
            userId,
            Instant.now()));
  }

  private void requireRecommendation(
      UUID userId, UUID organisationId, UUID projectId, UUID recommendationId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    recommendationRepository
        .findById(recommendationId)
        .filter(
            item ->
                organisationId.equals(item.getOrganisationId())
                    && projectId.equals(item.getProjectId()))
        .orElseThrow(
            () -> new ReviewAccessException(HttpStatus.NOT_FOUND, "RECOMMENDATION_NOT_FOUND"));
  }

  public static class ReviewAccessException extends RuntimeException {
    private final HttpStatus status;

    public ReviewAccessException(HttpStatus status, String message) {
      super(message);
      this.status = status;
    }

    public HttpStatus getStatus() {
      return status;
    }
  }
}
