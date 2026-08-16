package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.samharrison.incidentresponse.ingestion.NormalisedEventType;
import com.samharrison.incidentresponse.ingestion.PipelineRunStatus;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IncidentModelCoverageTest {

  @Test
  void coversAllOpenIncidentTransitionStates() {
    Incident incident = incident();
    Instant time = Instant.parse("2026-08-16T10:00:00Z");

    incident.transitionTo(IncidentStatus.TRIAGED, time);
    incident.transitionTo(IncidentStatus.MITIGATING, time.plusSeconds(1));
    incident.transitionTo(IncidentStatus.MONITORING, time.plusSeconds(2));
    incident.transitionTo(IncidentStatus.MITIGATING, time.plusSeconds(3));
    incident.transitionTo(IncidentStatus.MONITORING, time.plusSeconds(4));
    incident.transitionTo(IncidentStatus.RESOLVED, time.plusSeconds(5));
    incident.transitionTo(IncidentStatus.REOPENED, time.plusSeconds(6));
    incident.transitionTo(IncidentStatus.TRIAGED, time.plusSeconds(7));

    assertThat(incident.getStatus()).isEqualTo(IncidentStatus.TRIAGED);
    assertThat(incident.getResolvedAt()).isNull();
  }

  @Test
  void scoresOpenAndRejectsResolvedOrCrossTenantCandidates() {
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    CorrelationEvent event =
        new CorrelationEvent(
            UUID.randomUUID(),
            organisationId,
            projectId,
            Instant.parse("2026-08-16T10:00:00Z"),
            NormalisedEventType.PIPELINE_RUN_COMPLETED,
            PipelineRunStatus.FAILED,
            "commit",
            "production",
            "run-1",
            1);
    IncidentCorrelationCandidate open =
        candidate(organisationId, projectId, IncidentStatus.DETECTED);
    IncidentCorrelationCandidate resolved =
        candidate(organisationId, projectId, IncidentStatus.RESOLVED);
    IncidentCorrelationCandidate otherTenant =
        candidate(UUID.randomUUID(), projectId, IncidentStatus.DETECTED);

    assertThat(new DeterministicCorrelationPolicy().score(event, open).score()).isGreaterThan(0);
    assertThat(new DeterministicCorrelationPolicy().score(event, resolved).score()).isZero();
    assertThat(new DeterministicCorrelationPolicy().score(event, otherTenant).score()).isZero();
    assertThat(new DeterministicCorrelationPolicy().isEligible(event)).isTrue();
    assertThat(
            new DeterministicCorrelationPolicy()
                .isEligible(
                    new CorrelationEvent(
                        UUID.randomUUID(),
                        organisationId,
                        projectId,
                        event.occurredAt(),
                        event.eventType(),
                        PipelineRunStatus.SUCCEEDED,
                        "commit",
                        "production",
                        "run-1",
                        1)))
        .isFalse();
  }

  @Test
  void exercisesCorrelationDimensionsAndEligibilityBranches() {
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Instant occurredAt = Instant.parse("2026-08-16T10:00:00Z");
    DeterministicCorrelationPolicy policy = new DeterministicCorrelationPolicy();
    CorrelationEvent event =
        new CorrelationEvent(
            UUID.randomUUID(),
            organisationId,
            projectId,
            occurredAt,
            NormalisedEventType.PIPELINE_RUN_COMPLETED,
            PipelineRunStatus.FAILED,
            "commit",
            "production",
            "run-1",
            1);

    assertThat(
            policy
                .score(event, candidate(UUID.randomUUID(), projectId, IncidentStatus.DETECTED))
                .score())
        .isZero();
    assertThat(
            policy
                .score(
                    event,
                    new IncidentCorrelationCandidate(
                        UUID.randomUUID(),
                        organisationId,
                        UUID.randomUUID(),
                        IncidentStatus.DETECTED,
                        occurredAt,
                        NormalisedEventType.PIPELINE_RUN_COMPLETED,
                        "commit",
                        "production",
                        "run-1",
                        1))
                .score())
        .isZero();
    assertThat(
            policy
                .score(
                    event,
                    new IncidentCorrelationCandidate(
                        UUID.randomUUID(),
                        organisationId,
                        projectId,
                        IncidentStatus.DETECTED,
                        occurredAt.plusSeconds(3601),
                        NormalisedEventType.DEPLOYMENT_COMPLETED,
                        "different",
                        "staging",
                        "run-2",
                        2))
                .score())
        .isZero();
    assertThat(
            policy
                .score(
                    event,
                    new IncidentCorrelationCandidate(
                        UUID.randomUUID(),
                        organisationId,
                        projectId,
                        IncidentStatus.DETECTED,
                        occurredAt,
                        NormalisedEventType.DEPLOYMENT_COMPLETED,
                        null,
                        null,
                        "run-2",
                        2))
                .score())
        .isEqualTo(1);
    assertThat(
            policy
                .score(
                    new CorrelationEvent(
                        UUID.randomUUID(),
                        organisationId,
                        projectId,
                        occurredAt,
                        NormalisedEventType.PIPELINE_RUN_COMPLETED,
                        PipelineRunStatus.FAILED,
                        null,
                        "production",
                        "run-1",
                        1),
                    candidate(organisationId, projectId, IncidentStatus.DETECTED, "run-1", 1))
                .score())
        .isGreaterThanOrEqualTo(1);
    assertThat(
            policy
                .score(
                    new CorrelationEvent(
                        UUID.randomUUID(),
                        organisationId,
                        projectId,
                        occurredAt,
                        NormalisedEventType.PIPELINE_RUN_COMPLETED,
                        PipelineRunStatus.FAILED,
                        " ",
                        "production",
                        "run-1",
                        1),
                    candidate(organisationId, projectId, IncidentStatus.DETECTED, "run-1", 1))
                .score())
        .isGreaterThanOrEqualTo(1);
    assertThat(
            policy
                .score(
                    event,
                    candidate(organisationId, projectId, IncidentStatus.DETECTED, "run-1", 2))
                .score())
        .isGreaterThanOrEqualTo(1);

    for (PipelineRunStatus status :
        new PipelineRunStatus[] {PipelineRunStatus.CANCELLED, PipelineRunStatus.TIMED_OUT}) {
      assertThat(
              policy.isEligible(
                  new CorrelationEvent(
                      UUID.randomUUID(),
                      organisationId,
                      projectId,
                      occurredAt,
                      NormalisedEventType.PIPELINE_RUN_COMPLETED,
                      status,
                      "commit",
                      null,
                      "run-1",
                      1)))
          .isTrue();
    }
  }

  @Test
  void coversNullExternalRunValidationBranches() {
    assertThatThrownBy(
            () ->
                new CorrelationEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now(),
                    NormalisedEventType.PIPELINE_RUN_COMPLETED,
                    PipelineRunStatus.FAILED,
                    null,
                    null,
                    null,
                    1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new IncidentCorrelationCandidate(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    IncidentStatus.DETECTED,
                    Instant.now(),
                    NormalisedEventType.PIPELINE_RUN_COMPLETED,
                    null,
                    null,
                    null,
                    1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsInvalidCorrelationRecords() {
    assertThatThrownBy(
            () ->
                new CorrelationEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now(),
                    NormalisedEventType.PIPELINE_RUN_COMPLETED,
                    PipelineRunStatus.FAILED,
                    null,
                    null,
                    "",
                    1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new CorrelationEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    Instant.now(),
                    NormalisedEventType.PIPELINE_RUN_COMPLETED,
                    PipelineRunStatus.FAILED,
                    null,
                    null,
                    "run",
                    0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> candidate(UUID.randomUUID(), UUID.randomUUID(), IncidentStatus.DETECTED, "", 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                candidate(UUID.randomUUID(), UUID.randomUUID(), IncidentStatus.DETECTED, "run", 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static Incident incident() {
    return new Incident(
        UUID.randomUUID(),
        mock(Organisation.class),
        mock(Project.class),
        "Incident",
        "Summary",
        Instant.now());
  }

  private static IncidentCorrelationCandidate candidate(
      UUID organisationId, UUID projectId, IncidentStatus status) {
    return candidate(organisationId, projectId, status, "run-1", 1);
  }

  private static IncidentCorrelationCandidate candidate(
      UUID organisationId, UUID projectId, IncidentStatus status, String run, int attempt) {
    return new IncidentCorrelationCandidate(
        UUID.randomUUID(),
        organisationId,
        projectId,
        status,
        Instant.parse("2026-08-16T09:59:00Z"),
        NormalisedEventType.PIPELINE_RUN_COMPLETED,
        "commit",
        "production",
        run,
        attempt);
  }
}
