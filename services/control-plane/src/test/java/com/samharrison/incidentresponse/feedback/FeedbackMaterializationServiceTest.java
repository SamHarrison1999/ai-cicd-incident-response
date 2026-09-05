package com.samharrison.incidentresponse.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.learning.OperationalLearningMaterializationService;
import com.samharrison.incidentresponse.review.IncidentResolution;
import com.samharrison.incidentresponse.review.RecommendationReview;
import com.samharrison.incidentresponse.review.RecommendationReviewRepository;
import com.samharrison.incidentresponse.review.ReviewAction;
import com.samharrison.incidentresponse.review.ReviewReason;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FeedbackMaterializationServiceTest {

  @Test
  void materializesAllReviewOutcomesAndRemainsIdempotent() {
    FeedbackSignalRepository signalRepository = mock(FeedbackSignalRepository.class);
    FeedbackAggregateRepository aggregateRepository = mock(FeedbackAggregateRepository.class);
    RecommendationReviewRepository reviewRepository = mock(RecommendationReviewRepository.class);
    OperationalLearningMaterializationService learningMaterializationService =
        mock(OperationalLearningMaterializationService.class);

    List<FeedbackSignal> signals = new ArrayList<>();
    List<FeedbackAggregate> aggregates = new ArrayList<>();

    configureRepositories(signalRepository, aggregateRepository, signals, aggregates);

    FeedbackMaterializationService service =
        new FeedbackMaterializationService(
            signalRepository,
            aggregateRepository,
            new DeterministicFeedbackAggregationService(),
            reviewRepository,
            learningMaterializationService);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID recommendationId = UUID.randomUUID();

    RecommendationReview accepted =
        review(
            organisationId,
            projectId,
            recommendationId,
            null,
            ReviewAction.ACCEPT,
            ReviewReason.NONE,
            Instant.parse("2026-09-05T10:00:00Z"));

    RecommendationReview edited =
        review(
            organisationId,
            projectId,
            recommendationId,
            UUID.randomUUID(),
            ReviewAction.EDIT,
            ReviewReason.NONE,
            Instant.parse("2026-09-05T11:00:00Z"));

    RecommendationReview rejected =
        review(
            organisationId,
            projectId,
            recommendationId,
            null,
            ReviewAction.REJECT,
            ReviewReason.OTHER,
            Instant.parse("2026-09-05T12:00:00Z"));

    service.recordReview(accepted);
    service.recordReview(edited);
    service.recordReview(rejected);

    assertThat(signals)
        .extracting(FeedbackSignal::getOutcome)
        .containsExactly(
            FeedbackOutcome.ACCEPTED, FeedbackOutcome.EDITED, FeedbackOutcome.REJECTED);

    FeedbackAggregate aggregate = aggregates.get(0);

    assertThat(aggregate.getSampleCount()).isEqualTo(3);
    assertThat(aggregate.getAcceptedCount()).isEqualTo(1);
    assertThat(aggregate.getEditedCount()).isEqualTo(1);
    assertThat(aggregate.getRejectedCount()).isEqualTo(1);
    assertThat(aggregate.getResolvedCount()).isZero();
    assertThat(aggregate.getSuppressionReason()).isEqualTo(FeedbackSuppressionReason.NONE);
    assertThat(aggregate.getPolicyVersion())
        .isEqualTo(FeedbackMaterializationService.POLICY_VERSION);

    UUID aggregateId = aggregate.getId();

    service.recordReview(accepted);

    assertThat(signals).hasSize(3);
    assertThat(aggregates).hasSize(1);
    assertThat(aggregates.get(0).getId()).isEqualTo(aggregateId);
    assertThat(aggregates.get(0).getSampleCount()).isEqualTo(3);
  }

  @Test
  void materializesResolutionAgainstItsGoverningReviewAndRejectsOrphans() {
    FeedbackSignalRepository signalRepository = mock(FeedbackSignalRepository.class);
    FeedbackAggregateRepository aggregateRepository = mock(FeedbackAggregateRepository.class);
    RecommendationReviewRepository reviewRepository = mock(RecommendationReviewRepository.class);
    OperationalLearningMaterializationService learningMaterializationService =
        mock(OperationalLearningMaterializationService.class);

    List<FeedbackSignal> signals = new ArrayList<>();
    List<FeedbackAggregate> aggregates = new ArrayList<>();

    configureRepositories(signalRepository, aggregateRepository, signals, aggregates);

    FeedbackMaterializationService service =
        new FeedbackMaterializationService(
            signalRepository,
            aggregateRepository,
            new DeterministicFeedbackAggregationService(),
            reviewRepository,
            learningMaterializationService);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID recommendationId = UUID.randomUUID();
    UUID reviewedVersionId = UUID.randomUUID();

    RecommendationReview unrelated =
        review(
            organisationId,
            projectId,
            recommendationId,
            UUID.randomUUID(),
            ReviewAction.EDIT,
            ReviewReason.NONE,
            Instant.parse("2026-09-05T09:00:00Z"));

    RecommendationReview governing =
        review(
            organisationId,
            projectId,
            recommendationId,
            reviewedVersionId,
            ReviewAction.EDIT,
            ReviewReason.NONE,
            Instant.parse("2026-09-05T10:00:00Z"));

    when(reviewRepository
            .findAllByOrganisationIdAndProjectIdAndRecommendationIdOrderByCreatedAtDescIdDesc(
                organisationId, projectId, recommendationId))
        .thenReturn(List.of(unrelated, governing));

    service.recordReview(governing);

    IncidentResolution resolution =
        new IncidentResolution(
            UUID.randomUUID(),
            organisationId,
            projectId,
            UUID.randomUUID(),
            recommendationId,
            reviewedVersionId,
            "Human reviewer confirmed the bounded resolution.",
            UUID.randomUUID(),
            Instant.parse("2026-09-05T11:00:00Z"));

    service.recordResolution(resolution);

    assertThat(signals)
        .extracting(FeedbackSignal::getOutcome)
        .containsExactly(FeedbackOutcome.EDITED, FeedbackOutcome.RESOLVED);

    FeedbackAggregate aggregate = aggregates.get(0);

    assertThat(aggregate.getSampleCount()).isEqualTo(2);
    assertThat(aggregate.getEditedCount()).isEqualTo(1);
    assertThat(aggregate.getResolvedCount()).isEqualTo(1);
    assertThat(aggregate.getSuppressionReason())
        .isEqualTo(FeedbackSuppressionReason.INSUFFICIENT_SAMPLE);

    service.recordResolution(resolution);

    assertThat(signals).hasSize(2);
    assertThat(aggregates).hasSize(1);

    when(reviewRepository
            .findAllByOrganisationIdAndProjectIdAndRecommendationIdOrderByCreatedAtDescIdDesc(
                organisationId, projectId, recommendationId))
        .thenReturn(List.of(unrelated));

    assertThatThrownBy(() -> service.recordResolution(resolution))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no governing review");
  }

  private static RecommendationReview review(
      UUID organisationId,
      UUID projectId,
      UUID recommendationId,
      UUID reviewedVersionId,
      ReviewAction action,
      ReviewReason reason,
      Instant createdAt) {
    return new RecommendationReview(
        UUID.randomUUID(),
        organisationId,
        projectId,
        recommendationId,
        reviewedVersionId,
        action,
        reason,
        null,
        UUID.randomUUID(),
        createdAt);
  }

  private static void configureRepositories(
      FeedbackSignalRepository signalRepository,
      FeedbackAggregateRepository aggregateRepository,
      List<FeedbackSignal> signals,
      List<FeedbackAggregate> aggregates) {
    when(signalRepository.existsById(any()))
        .thenAnswer(
            invocation -> {
              UUID id = invocation.getArgument(0);
              return signals.stream().anyMatch(signal -> signal.getId().equals(id));
            });

    when(signalRepository.save(any()))
        .thenAnswer(
            invocation -> {
              FeedbackSignal signal = invocation.getArgument(0);
              signals.removeIf(existing -> existing.getId().equals(signal.getId()));
              signals.add(signal);
              signals.sort(
                  java.util.Comparator.comparing(FeedbackSignal::getCreatedAt)
                      .thenComparing(FeedbackSignal::getId));
              return signal;
            });

    when(signalRepository.findAllByOrganisationIdAndProjectIdOrderByCreatedAtAscIdAsc(any(), any()))
        .thenAnswer(invocation -> List.copyOf(signals));

    when(aggregateRepository.save(any()))
        .thenAnswer(
            invocation -> {
              FeedbackAggregate aggregate = invocation.getArgument(0);
              aggregates.removeIf(existing -> existing.getId().equals(aggregate.getId()));
              aggregates.add(aggregate);
              return aggregate;
            });
  }
}
