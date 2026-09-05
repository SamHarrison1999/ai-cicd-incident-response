package com.samharrison.incidentresponse.feedback;

import com.samharrison.incidentresponse.learning.OperationalLearningMaterializationService;
import com.samharrison.incidentresponse.review.IncidentResolution;
import com.samharrison.incidentresponse.review.RecommendationReview;
import com.samharrison.incidentresponse.review.RecommendationReviewRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FeedbackMaterializationService {
  public static final String POLICY_VERSION = "governed-outcomes-v1";

  private final FeedbackSignalRepository signalRepository;
  private final FeedbackAggregateRepository aggregateRepository;
  private final DeterministicFeedbackAggregationService aggregationService;
  private final RecommendationReviewRepository reviewRepository;
  private final OperationalLearningMaterializationService learningMaterializationService;

  public FeedbackMaterializationService(
      FeedbackSignalRepository signalRepository,
      FeedbackAggregateRepository aggregateRepository,
      DeterministicFeedbackAggregationService aggregationService,
      RecommendationReviewRepository reviewRepository,
      OperationalLearningMaterializationService learningMaterializationService) {
    this.signalRepository = signalRepository;
    this.aggregateRepository = aggregateRepository;
    this.aggregationService = aggregationService;
    this.reviewRepository = reviewRepository;
    this.learningMaterializationService = learningMaterializationService;
  }

  public void recordReview(RecommendationReview review) {
    FeedbackOutcome outcome =
        switch (review.getAction()) {
          case ACCEPT -> FeedbackOutcome.ACCEPTED;
          case EDIT -> FeedbackOutcome.EDITED;
          case REJECT -> FeedbackOutcome.REJECTED;
        };

    materialize(
        review.getOrganisationId(),
        review.getProjectId(),
        review.getRecommendationId(),
        review.getId(),
        outcome,
        review.getCreatedAt());
  }

  public void recordResolution(IncidentResolution resolution) {
    RecommendationReview governingReview =
        reviewRepository
            .findAllByOrganisationIdAndProjectIdAndRecommendationIdOrderByCreatedAtDescIdDesc(
                resolution.getOrganisationId(),
                resolution.getProjectId(),
                resolution.getRecommendationId())
            .stream()
            .filter(
                review -> resolution.getReviewedVersionId().equals(review.getReviewedVersionId()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "reviewed recommendation version has no governing review"));

    materialize(
        resolution.getOrganisationId(),
        resolution.getProjectId(),
        resolution.getRecommendationId(),
        governingReview.getId(),
        FeedbackOutcome.RESOLVED,
        resolution.getCreatedAt());
  }

  private void materialize(
      UUID organisationId,
      UUID projectId,
      UUID recommendationId,
      UUID reviewId,
      FeedbackOutcome outcome,
      Instant createdAt) {
    UUID signalId = deterministicId("feedback-signal", reviewId.toString(), outcome.name());

    if (!signalRepository.existsById(signalId)) {
      signalRepository.save(
          new FeedbackSignal(
              signalId,
              organisationId,
              projectId,
              recommendationId,
              reviewId,
              outcome,
              POLICY_VERSION,
              createdAt));
    }

    List<FeedbackSignal> signals =
        signalRepository.findAllByOrganisationIdAndProjectIdOrderByCreatedAtAscIdAsc(
            organisationId, projectId);

    List<FeedbackSignal> policySignals =
        signals.stream()
            .filter(signal -> POLICY_VERSION.equals(signal.getPolicyVersion()))
            .sorted(
                Comparator.comparing(FeedbackSignal::getCreatedAt)
                    .thenComparing(FeedbackSignal::getId))
            .toList();

    Instant windowStart = policySignals.get(0).getCreatedAt();
    Instant windowEnd = policySignals.get(policySignals.size() - 1).getCreatedAt();

    FeedbackAggregate calculated =
        aggregationService.aggregate(
            organisationId, projectId, POLICY_VERSION, windowStart, windowEnd, signals);

    UUID aggregateId =
        deterministicId(
            "feedback-aggregate", organisationId.toString(), projectId.toString(), POLICY_VERSION);

    aggregateRepository.save(
        new FeedbackAggregate(
            aggregateId,
            organisationId,
            projectId,
            POLICY_VERSION,
            windowStart,
            windowEnd,
            calculated.getSampleCount(),
            calculated.getAcceptedCount(),
            calculated.getEditedCount(),
            calculated.getRejectedCount(),
            calculated.getResolvedCount(),
            calculated.getSuppressionReason()));

    learningMaterializationService.materialize(organisationId, projectId);
  }

  private static UUID deterministicId(String... parts) {
    return UUID.nameUUIDFromBytes(String.join(":", parts).getBytes(StandardCharsets.UTF_8));
  }
}
