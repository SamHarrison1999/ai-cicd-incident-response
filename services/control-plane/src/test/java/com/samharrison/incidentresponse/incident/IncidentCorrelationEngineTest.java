package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.samharrison.incidentresponse.ingestion.NormalisedEventType;
import com.samharrison.incidentresponse.ingestion.PipelineRunStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IncidentCorrelationEngineTest {

  private final IncidentCorrelationEngine engine = new IncidentCorrelationEngine();

  @Test
  void selectsTheHighestScoringCandidateWithinTheTenant() {
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID selectedId = UUID.randomUUID();
    CorrelationEvent event = newEvent(organisationId, projectId, "commit-1", "prod");
    IncidentCorrelationCandidate selected =
        candidate(
            selectedId,
            organisationId,
            projectId,
            Instant.parse("2026-08-14T11:00:00Z"),
            "commit-1",
            "prod");

    CorrelationDecision decision = engine.evaluate(event, List.of(selected));

    assertThat(decision.selectedIncidentId()).isEqualTo(selectedId);
    assertThat(decision.score()).isEqualTo(9);
    assertThat(decision.policyVersion()).isEqualTo("incident-correlation-v1");
    assertThat(decision.matchedDimensions())
        .containsExactlyInAnyOrder(
            CorrelationDimension.COMMIT,
            CorrelationDimension.ENVIRONMENT,
            CorrelationDimension.EVENT_FAMILY,
            CorrelationDimension.PIPELINE_RUN,
            CorrelationDimension.TIME_WINDOW);
  }

  @Test
  void resolvesEqualScoresByCreationTimeThenIncidentId() {
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID earlierId = UUID.randomUUID();
    UUID laterId = UUID.randomUUID();
    Instant detectedAt = Instant.parse("2026-08-14T11:00:00Z");
    CorrelationEvent event = newEvent(organisationId, projectId, "commit-1", null);

    CorrelationDecision decision =
        engine.evaluate(
            event,
            List.of(
                candidate(
                    laterId,
                    organisationId,
                    projectId,
                    detectedAt.plusSeconds(10),
                    "commit-1",
                    null),
                candidate(earlierId, organisationId, projectId, detectedAt, "commit-1", null)));

    assertThat(decision.selectedIncidentId()).isEqualTo(earlierId);

    UUID firstId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID secondId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    CorrelationDecision sameTimeDecision =
        engine.evaluate(
            event,
            List.of(
                candidate(secondId, organisationId, projectId, detectedAt, "commit-1", null),
                candidate(firstId, organisationId, projectId, detectedAt, "commit-1", null)));
    assertThat(sameTimeDecision.selectedIncidentId()).isEqualTo(firstId);
  }

  @Test
  void rejectsNonFailureEventsAndCrossTenantCandidates() {
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    CorrelationEvent successfulEvent =
        new CorrelationEvent(
            UUID.randomUUID(),
            organisationId,
            projectId,
            Instant.parse("2026-08-14T11:00:00Z"),
            NormalisedEventType.PIPELINE_RUN_COMPLETED,
            PipelineRunStatus.SUCCEEDED,
            "commit-1",
            "prod",
            "run-1",
            1);

    CorrelationDecision decision =
        engine.evaluate(
            successfulEvent,
            List.of(
                candidate(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    projectId,
                    Instant.parse("2026-08-14T11:00:00Z"),
                    "commit-1",
                    "prod")));

    assertThat(decision.eligible()).isFalse();
    assertThat(decision.selectedIncidentId()).isNull();
    assertThat(decision.consideredCandidates()).isEmpty();

    CorrelationEvent failedEvent = newEvent(organisationId, projectId, "commit-1", "prod");
    CorrelationDecision differentProjectDecision =
        engine.evaluate(
            failedEvent,
            List.of(
                candidate(
                    UUID.randomUUID(),
                    organisationId,
                    UUID.randomUUID(),
                    Instant.parse("2026-08-14T11:00:00Z"),
                    "commit-1",
                    "prod")));
    assertThat(differentProjectDecision.consideredCandidates()).isEmpty();
  }

  @Test
  void comparesCandidatesWithDifferentScoresBeforeTieBreakers() {
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    CorrelationEvent event = newEvent(organisationId, projectId, "commit-1", "prod");
    IncidentCorrelationCandidate lowerScore =
        candidate(
            UUID.randomUUID(),
            organisationId,
            projectId,
            Instant.parse("2026-08-14T11:00:00Z"),
            "different",
            null);
    IncidentCorrelationCandidate higherScore =
        candidate(
            UUID.randomUUID(),
            organisationId,
            projectId,
            Instant.parse("2026-08-14T11:00:00Z"),
            "commit-1",
            null);

    assertThat(engine.evaluate(event, List.of(lowerScore, higherScore)).selectedIncidentId())
        .isEqualTo(higherScore.incidentId());
  }

  private static CorrelationEvent newEvent(
      UUID organisationId, UUID projectId, String commitSha, String environmentName) {
    return new CorrelationEvent(
        UUID.randomUUID(),
        organisationId,
        projectId,
        Instant.parse("2026-08-14T11:10:00Z"),
        NormalisedEventType.PIPELINE_RUN_COMPLETED,
        PipelineRunStatus.FAILED,
        commitSha,
        environmentName,
        "run-1",
        1);
  }

  private static IncidentCorrelationCandidate candidate(
      UUID incidentId,
      UUID organisationId,
      UUID projectId,
      Instant detectedAt,
      String commitSha,
      String environmentName) {
    return new IncidentCorrelationCandidate(
        incidentId,
        organisationId,
        projectId,
        IncidentStatus.DETECTED,
        detectedAt,
        NormalisedEventType.PIPELINE_RUN_COMPLETED,
        commitSha,
        environmentName,
        "run-1",
        1);
  }
}
