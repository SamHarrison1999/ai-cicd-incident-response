package com.samharrison.incidentresponse.review;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.recommendation.Recommendation;
import com.samharrison.incidentresponse.recommendation.RecommendationRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ReviewDeepCoverageTest {
  private final UUID userId = UUID.randomUUID();
  private final UUID organisationId = UUID.randomUUID();
  private final UUID projectId = UUID.randomUUID();
  private final UUID recommendationId = UUID.randomUUID();

  @Test
  void reviewServiceCoversAcceptEditVersionsAndMissingRecommendation() {
    RecommendationRepository recommendationRepository = mock(RecommendationRepository.class);
    RecommendationReviewRepository reviewRepository = mock(RecommendationReviewRepository.class);
    ReviewedRecommendationVersionRepository versionRepository =
        mock(ReviewedRecommendationVersionRepository.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    Recommendation recommendation = mock(Recommendation.class);
    RecommendationReview savedReview = mock(RecommendationReview.class);
    ReviewedRecommendationVersion savedVersion = mock(ReviewedRecommendationVersion.class);
    when(recommendation.getId()).thenReturn(recommendationId);
    when(recommendation.getOrganisationId()).thenReturn(organisationId);
    when(recommendation.getProjectId()).thenReturn(projectId);
    when(recommendationRepository.findById(recommendationId))
        .thenReturn(Optional.of(recommendation));
    when(reviewRepository.save(any())).thenReturn(savedReview);
    when(versionRepository.save(any())).thenReturn(savedVersion);
    when(savedVersion.getId()).thenReturn(UUID.randomUUID());

    RecommendationReviewService service =
        new RecommendationReviewService(
            recommendationRepository, reviewRepository, versionRepository, tenantAccessService);
    service.review(
        userId,
        organisationId,
        projectId,
        recommendationId,
        ReviewAction.ACCEPT,
        ReviewReason.NONE,
        null,
        null,
        null);
    service.review(
        userId,
        organisationId,
        projectId,
        recommendationId,
        ReviewAction.EDIT,
        ReviewReason.NONE,
        "comment",
        "edited summary",
        "edited cause");

    ReviewedRecommendationVersion existing = mock(ReviewedRecommendationVersion.class);
    when(existing.getVersionNumber()).thenReturn(3);
    when(versionRepository
            .findTopByOrganisationIdAndProjectIdAndRecommendationIdOrderByVersionNumberDesc(
                organisationId, projectId, recommendationId))
        .thenReturn(Optional.of(existing));
    service.review(
        userId,
        organisationId,
        projectId,
        recommendationId,
        ReviewAction.EDIT,
        ReviewReason.OTHER,
        "comment",
        "edited summary 2",
        null);

    when(recommendationRepository.findById(recommendationId)).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                service.review(
                    userId,
                    organisationId,
                    projectId,
                    recommendationId,
                    ReviewAction.ACCEPT,
                    ReviewReason.NONE,
                    null,
                    null,
                    null))
        .isInstanceOf(RecommendationReviewService.ReviewAccessException.class);
    when(recommendationRepository.findById(recommendationId))
        .thenReturn(Optional.of(recommendation));
    when(recommendation.getOrganisationId()).thenReturn(UUID.randomUUID());
    assertThatThrownBy(
            () ->
                service.review(
                    userId,
                    organisationId,
                    projectId,
                    recommendationId,
                    ReviewAction.ACCEPT,
                    ReviewReason.NONE,
                    null,
                    null,
                    null))
        .isInstanceOf(RecommendationReviewService.ReviewAccessException.class);
    when(recommendation.getOrganisationId()).thenReturn(organisationId);
    when(recommendationRepository.findById(recommendationId))
        .thenReturn(Optional.of(recommendation));
    assertThatThrownBy(
            () ->
                service.review(
                    userId,
                    organisationId,
                    projectId,
                    recommendationId,
                    ReviewAction.EDIT,
                    ReviewReason.NONE,
                    null,
                    "",
                    null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                service.review(
                    userId,
                    organisationId,
                    projectId,
                    recommendationId,
                    ReviewAction.EDIT,
                    ReviewReason.NONE,
                    null,
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void reviewApiCoversHistorySubmitResolveAndAccessFailures() {
    RecommendationRepository recommendationRepository = mock(RecommendationRepository.class);
    RecommendationReviewRepository reviewRepository = mock(RecommendationReviewRepository.class);
    ReviewedRecommendationVersionRepository versionRepository =
        mock(ReviewedRecommendationVersionRepository.class);
    IncidentResolutionRepository resolutionRepository = mock(IncidentResolutionRepository.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    Recommendation recommendation = mock(Recommendation.class);
    ReviewedRecommendationVersion version = mock(ReviewedRecommendationVersion.class);
    when(recommendation.getId()).thenReturn(recommendationId);
    when(recommendation.getOrganisationId()).thenReturn(organisationId);
    when(recommendation.getProjectId()).thenReturn(projectId);
    when(recommendationRepository.findById(recommendationId))
        .thenReturn(Optional.of(recommendation));
    when(reviewRepository
            .findAllByOrganisationIdAndProjectIdAndRecommendationIdOrderByCreatedAtDescIdDesc(
                organisationId, projectId, recommendationId))
        .thenReturn(List.of());
    when(versionRepository.findById(any())).thenReturn(Optional.of(version));
    when(version.getId()).thenReturn(UUID.randomUUID());
    when(version.getOrganisationId()).thenReturn(organisationId);
    when(version.getProjectId()).thenReturn(projectId);
    when(version.getRecommendationId()).thenReturn(recommendationId);
    when(recommendationRepository.existsById(recommendationId)).thenReturn(true);
    when(resolutionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ReviewApiService service =
        new ReviewApiService(
            recommendationRepository,
            reviewRepository,
            versionRepository,
            resolutionRepository,
            tenantAccessService);
    service.history(userId, organisationId, projectId, recommendationId);
    service.submit(
        userId,
        organisationId,
        projectId,
        recommendationId,
        ReviewAction.ACCEPT,
        ReviewReason.NONE,
        null,
        null,
        null);
    service.resolve(
        userId,
        organisationId,
        projectId,
        UUID.randomUUID(),
        recommendationId,
        UUID.randomUUID(),
        "resolution");

    when(versionRepository.findById(any())).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                service.resolve(
                    userId,
                    organisationId,
                    projectId,
                    UUID.randomUUID(),
                    recommendationId,
                    UUID.randomUUID(),
                    "resolution"))
        .isInstanceOf(ReviewApiService.ReviewAccessException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.NOT_FOUND);
    when(versionRepository.findById(any())).thenReturn(Optional.of(version));
    when(recommendationRepository.existsById(recommendationId)).thenReturn(false);
    assertThatThrownBy(
            () ->
                service.resolve(
                    userId,
                    organisationId,
                    projectId,
                    UUID.randomUUID(),
                    recommendationId,
                    UUID.randomUUID(),
                    "resolution"))
        .isInstanceOf(ReviewApiService.ReviewAccessException.class);
    when(version.getOrganisationId()).thenReturn(UUID.randomUUID());
    assertThatThrownBy(
            () ->
                service.resolve(
                    userId,
                    organisationId,
                    projectId,
                    UUID.randomUUID(),
                    recommendationId,
                    UUID.randomUUID(),
                    "resolution"))
        .isInstanceOf(ReviewApiService.ReviewAccessException.class);

    when(version.getOrganisationId()).thenReturn(organisationId);
    when(version.getProjectId()).thenReturn(projectId);
    when(version.getRecommendationId()).thenReturn(UUID.randomUUID());
    assertThatThrownBy(
            () ->
                service.resolve(
                    userId,
                    organisationId,
                    projectId,
                    UUID.randomUUID(),
                    recommendationId,
                    UUID.randomUUID(),
                    "resolution"))
        .isInstanceOf(ReviewApiService.ReviewAccessException.class);
    verify(tenantAccessService, org.mockito.Mockito.atLeastOnce())
        .requireActiveMembership(organisationId, userId);
  }

  @Test
  void rejectsCrossTenantReviewAndResolutionReferences() {
    RecommendationRepository recommendationRepository = mock(RecommendationRepository.class);
    RecommendationReviewRepository reviewRepository = mock(RecommendationReviewRepository.class);
    ReviewedRecommendationVersionRepository versionRepository =
        mock(ReviewedRecommendationVersionRepository.class);
    IncidentResolutionRepository resolutionRepository = mock(IncidentResolutionRepository.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    Recommendation recommendation = mock(Recommendation.class);
    ReviewedRecommendationVersion version = mock(ReviewedRecommendationVersion.class);
    when(recommendationRepository.findById(recommendationId))
        .thenReturn(Optional.of(recommendation));
    when(recommendation.getOrganisationId()).thenReturn(organisationId);
    when(recommendation.getProjectId()).thenReturn(projectId);

    RecommendationReviewService reviewService =
        new RecommendationReviewService(
            recommendationRepository, reviewRepository, versionRepository, tenantAccessService);
    assertThatThrownBy(
            () ->
                reviewService.review(
                    userId,
                    organisationId,
                    UUID.randomUUID(),
                    recommendationId,
                    ReviewAction.ACCEPT,
                    ReviewReason.NONE,
                    null,
                    null,
                    null))
        .isInstanceOf(RecommendationReviewService.ReviewAccessException.class);

    when(versionRepository.findById(any())).thenReturn(Optional.of(version));
    when(version.getOrganisationId()).thenReturn(organisationId);
    when(version.getProjectId()).thenReturn(UUID.randomUUID());
    when(version.getRecommendationId()).thenReturn(recommendationId);
    ReviewApiService apiService =
        new ReviewApiService(
            recommendationRepository,
            reviewRepository,
            versionRepository,
            resolutionRepository,
            tenantAccessService);
    assertThatThrownBy(
            () -> apiService.history(userId, organisationId, UUID.randomUUID(), recommendationId))
        .isInstanceOf(ReviewApiService.ReviewAccessException.class);
    when(recommendation.getOrganisationId()).thenReturn(UUID.randomUUID());
    assertThatThrownBy(
            () -> apiService.history(userId, organisationId, projectId, recommendationId))
        .isInstanceOf(ReviewApiService.ReviewAccessException.class);
    assertThatThrownBy(
            () ->
                apiService.resolve(
                    userId,
                    organisationId,
                    projectId,
                    UUID.randomUUID(),
                    recommendationId,
                    UUID.randomUUID(),
                    "resolution"))
        .isInstanceOf(ReviewApiService.ReviewAccessException.class);
  }
}
