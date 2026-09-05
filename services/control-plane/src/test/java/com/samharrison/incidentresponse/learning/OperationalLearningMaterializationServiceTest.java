package com.samharrison.incidentresponse.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.feedback.FeedbackMaterializationService;
import com.samharrison.incidentresponse.feedback.FeedbackOutcome;
import com.samharrison.incidentresponse.feedback.FeedbackSignal;
import com.samharrison.incidentresponse.feedback.FeedbackSignalRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OperationalLearningMaterializationServiceTest {

  @Test
  void materializesDailyGovernedOutcomeSnapshotsDeterministically() {
    FeedbackSignalRepository signalRepository = mock(FeedbackSignalRepository.class);
    OperationalTrendRepository trendRepository = mock(OperationalTrendRepository.class);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();

    FeedbackSignal previousDay =
        signal(
            organisationId,
            projectId,
            FeedbackOutcome.ACCEPTED,
            FeedbackMaterializationService.POLICY_VERSION,
            Instant.parse("2026-09-04T15:00:00Z"));

    FeedbackSignal acceptedOne =
        signal(
            organisationId,
            projectId,
            FeedbackOutcome.ACCEPTED,
            FeedbackMaterializationService.POLICY_VERSION,
            Instant.parse("2026-09-05T10:00:00Z"));

    FeedbackSignal edited =
        signal(
            organisationId,
            projectId,
            FeedbackOutcome.EDITED,
            FeedbackMaterializationService.POLICY_VERSION,
            Instant.parse("2026-09-05T10:30:00Z"));

    FeedbackSignal acceptedTwo =
        signal(
            organisationId,
            projectId,
            FeedbackOutcome.ACCEPTED,
            FeedbackMaterializationService.POLICY_VERSION,
            Instant.parse("2026-09-05T11:00:00Z"));

    FeedbackSignal acceptedThree =
        signal(
            organisationId,
            projectId,
            FeedbackOutcome.ACCEPTED,
            FeedbackMaterializationService.POLICY_VERSION,
            Instant.parse("2026-09-05T12:00:00Z"));

    FeedbackSignal ignoredPolicy =
        signal(
            organisationId,
            projectId,
            FeedbackOutcome.REJECTED,
            "legacy-policy",
            Instant.parse("2026-09-05T11:30:00Z"));

    when(signalRepository.findAllByOrganisationIdAndProjectIdOrderByCreatedAtAscIdAsc(
            organisationId, projectId))
        .thenReturn(
            List.of(acceptedTwo, ignoredPolicy, previousDay, acceptedThree, edited, acceptedOne));

    List<OperationalTrend> persisted = new ArrayList<>();

    when(trendRepository.save(any()))
        .thenAnswer(
            invocation -> {
              OperationalTrend trend = invocation.getArgument(0);
              persisted.removeIf(existing -> existing.getId().equals(trend.getId()));
              persisted.add(trend);
              return trend;
            });

    OperationalLearningMaterializationService service =
        new OperationalLearningMaterializationService(
            signalRepository, trendRepository, new DeterministicTrendObservationService());

    List<OperationalTrend> first = service.materialize(organisationId, projectId);

    assertThat(first)
        .extracting(OperationalTrend::getDimensionKey)
        .containsExactly("ACCEPTED", "ACCEPTED", "EDITED");

    OperationalTrend previousAccepted = first.get(0);
    OperationalTrend currentAccepted = first.get(1);
    OperationalTrend currentEdited = first.get(2);

    assertThat(previousAccepted.getSampleCount()).isEqualTo(1);
    assertThat(previousAccepted.getSuppressionReason())
        .isEqualTo(TrendSuppressionReason.INSUFFICIENT_SAMPLE);

    assertThat(currentAccepted.getWindowStart()).isEqualTo(Instant.parse("2026-09-05T10:00:00Z"));
    assertThat(currentAccepted.getWindowEnd()).isEqualTo(Instant.parse("2026-09-05T12:00:00Z"));
    assertThat(currentAccepted.getSampleCount()).isEqualTo(3);
    assertThat(currentAccepted.getObservedCount()).isEqualTo(3);
    assertThat(currentAccepted.getSuppressionReason()).isEqualTo(TrendSuppressionReason.NONE);
    assertThat(currentAccepted.getSourceReference())
        .isEqualTo("feedback-signal:" + acceptedOne.getId());
    assertThat(currentAccepted.getAggregationVersion())
        .isEqualTo(OperationalLearningMaterializationService.AGGREGATION_VERSION);
    assertThat(currentAccepted.getDimension()).isEqualTo(TrendDimension.RECOMMENDATION_OUTCOME);

    assertThat(currentEdited.getSampleCount()).isEqualTo(1);
    assertThat(currentEdited.getSuppressionReason())
        .isEqualTo(TrendSuppressionReason.INSUFFICIENT_SAMPLE);

    List<UUID> firstIds = first.stream().map(OperationalTrend::getId).toList();

    List<OperationalTrend> replay = service.materialize(organisationId, projectId);

    assertThat(replay.stream().map(OperationalTrend::getId).toList())
        .containsExactlyElementsOf(firstIds);
    assertThat(persisted).hasSize(3);
  }

  @Test
  void returnsEmptyWhenNoGovernedFeedbackExists() {
    FeedbackSignalRepository signalRepository = mock(FeedbackSignalRepository.class);
    OperationalTrendRepository trendRepository = mock(OperationalTrendRepository.class);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();

    when(signalRepository.findAllByOrganisationIdAndProjectIdOrderByCreatedAtAscIdAsc(
            organisationId, projectId))
        .thenReturn(
            List.of(
                signal(
                    organisationId,
                    projectId,
                    FeedbackOutcome.ACCEPTED,
                    "legacy-policy",
                    Instant.parse("2026-09-05T10:00:00Z"))));

    OperationalLearningMaterializationService service =
        new OperationalLearningMaterializationService(
            signalRepository, trendRepository, new DeterministicTrendObservationService());

    assertThat(service.materialize(organisationId, projectId)).isEmpty();

    verifyNoInteractions(trendRepository);
  }

  private static FeedbackSignal signal(
      UUID organisationId,
      UUID projectId,
      FeedbackOutcome outcome,
      String policyVersion,
      Instant createdAt) {
    return new FeedbackSignal(
        UUID.randomUUID(),
        organisationId,
        projectId,
        UUID.randomUUID(),
        UUID.randomUUID(),
        outcome,
        policyVersion,
        createdAt);
  }
}
