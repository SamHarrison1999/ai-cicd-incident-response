package com.samharrison.incidentresponse.review;

import com.samharrison.incidentresponse.recommendation.Recommendation;
import com.samharrison.incidentresponse.recommendation.RecommendationRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationReviewService {
  private final RecommendationRepository recommendationRepository;
  private final RecommendationReviewRepository reviewRepository;
  private final ReviewedRecommendationVersionRepository versionRepository;
  private final TenantAccessService tenantAccessService;

  public RecommendationReviewService(
      RecommendationRepository recommendationRepository,
      RecommendationReviewRepository reviewRepository,
      ReviewedRecommendationVersionRepository versionRepository,
      TenantAccessService tenantAccessService) {
    this.recommendationRepository = recommendationRepository;
    this.reviewRepository = reviewRepository;
    this.versionRepository = versionRepository;
    this.tenantAccessService = tenantAccessService;
  }

  @Transactional
  public RecommendationReview review(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      UUID recommendationId,
      ReviewAction action,
      ReviewReason reason,
      String comment,
      String editedSummary,
      String editedCause) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    Recommendation recommendation =
        recommendationRepository
            .findById(recommendationId)
            .filter(
                item ->
                    organisationId.equals(item.getOrganisationId())
                        && projectId.equals(item.getProjectId()))
            .orElseThrow(
                () -> new ReviewAccessException(HttpStatus.NOT_FOUND, "RECOMMENDATION_NOT_FOUND"));
    UUID versionId = null;
    if (action == ReviewAction.EDIT) {
      if (editedSummary == null || editedSummary.isBlank()) {
        throw new IllegalArgumentException("editedSummary is required for edits");
      }
      int version =
          versionRepository
              .findTopByOrganisationIdAndProjectIdAndRecommendationIdOrderByVersionNumberDesc(
                  organisationId, projectId, recommendationId)
              .map(existing -> existing.getVersionNumber() + 1)
              .orElse(1);
      ReviewedRecommendationVersion saved =
          versionRepository.save(
              new ReviewedRecommendationVersion(
                  UUID.randomUUID(),
                  organisationId,
                  projectId,
                  recommendation.getId(),
                  version,
                  editedSummary,
                  editedCause,
                  userId,
                  Instant.now()));
      versionId = saved.getId();
    }
    return reviewRepository.save(
        new RecommendationReview(
            UUID.randomUUID(),
            organisationId,
            projectId,
            recommendationId,
            versionId,
            action,
            reason,
            comment,
            userId,
            Instant.now()));
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
