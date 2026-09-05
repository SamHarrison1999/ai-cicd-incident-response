package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import com.samharrison.incidentresponse.ingestion.NormalisedEventType;
import com.samharrison.incidentresponse.ingestion.PipelineRunStatus;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class IncidentCorrelationWorkflowTest {

  @Test
  void returnsExistingIncidentWhenEventIsAlreadyLinked() {
    IncidentRepository incidentRepository = mock(IncidentRepository.class);
    IncidentEventLinkRepository eventLinkRepository = mock(IncidentEventLinkRepository.class);
    IncidentCorrelationDecisionRecorder decisionRecorder =
        mock(IncidentCorrelationDecisionRecorder.class);

    UUID eventId = UUID.randomUUID();
    NormalisedCiEvent event = mock(NormalisedCiEvent.class);
    Incident incident = mock(Incident.class);
    IncidentEventLink existingLink = mock(IncidentEventLink.class);

    when(event.getId()).thenReturn(eventId);
    when(existingLink.getIncident()).thenReturn(incident);
    when(eventLinkRepository.findByEventId(eventId)).thenReturn(Optional.of(existingLink));

    assertThat(workflow(incidentRepository, eventLinkRepository, decisionRecorder).correlate(event))
        .contains(incident);

    verify(incidentRepository, never())
        .findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(any(), any());
    verify(decisionRecorder, never()).record(any(), any(), any());
  }

  @Test
  void recordsIneligibleEventWithoutCreatingIncident() {
    IncidentRepository incidentRepository = mock(IncidentRepository.class);
    IncidentEventLinkRepository eventLinkRepository = mock(IncidentEventLinkRepository.class);
    IncidentCorrelationDecisionRecorder decisionRecorder =
        mock(IncidentCorrelationDecisionRecorder.class);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();

    Organisation organisation = organisation(organisationId);
    Project project = project(projectId);
    NormalisedCiEvent event =
        event(
            eventId,
            organisation,
            project,
            PipelineRunStatus.SUCCEEDED,
            "run-success",
            Instant.parse("2026-09-05T10:00:00Z"));

    when(eventLinkRepository.findByEventId(eventId)).thenReturn(Optional.empty());
    when(incidentRepository.findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(
            organisationId, projectId))
        .thenReturn(List.of());

    assertThat(workflow(incidentRepository, eventLinkRepository, decisionRecorder).correlate(event))
        .isEmpty();

    ArgumentCaptor<CorrelationDecision> decisionCaptor =
        ArgumentCaptor.forClass(CorrelationDecision.class);

    verify(decisionRecorder).record(eq(organisationId), eq(projectId), decisionCaptor.capture());

    assertThat(decisionCaptor.getValue().eligible()).isFalse();
    assertThat(decisionCaptor.getValue().selectedIncidentId()).isNull();

    verify(incidentRepository, never()).save(any(Incident.class));
    verify(eventLinkRepository, never()).save(any(IncidentEventLink.class));
  }

  @Test
  void createsIncidentForEligibleFailureWithoutCandidate() {
    IncidentRepository incidentRepository = mock(IncidentRepository.class);
    IncidentEventLinkRepository eventLinkRepository = mock(IncidentEventLinkRepository.class);
    IncidentCorrelationDecisionRecorder decisionRecorder =
        mock(IncidentCorrelationDecisionRecorder.class);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();

    Organisation organisation = organisation(organisationId);
    Project project = project(projectId);
    Instant occurredAt = Instant.parse("2026-09-05T10:00:00Z");

    NormalisedCiEvent event =
        event(eventId, organisation, project, PipelineRunStatus.FAILED, "run-failure", occurredAt);

    when(eventLinkRepository.findByEventId(eventId)).thenReturn(Optional.empty());
    when(incidentRepository.findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(
            organisationId, projectId))
        .thenReturn(List.of());

    Optional<Incident> result =
        workflow(incidentRepository, eventLinkRepository, decisionRecorder).correlate(event);

    ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
    verify(incidentRepository).save(incidentCaptor.capture());

    Incident created = incidentCaptor.getValue();

    assertThat(result).contains(created);
    assertThat(created.getStatus()).isEqualTo(IncidentStatus.DETECTED);
    assertThat(created.getTitle()).isEqualTo("portfolio-build");
    assertThat(created.getSummary()).isEqualTo("Pipeline failed during verification.");
    assertThat(created.getDetectedAt()).isEqualTo(occurredAt);

    ArgumentCaptor<IncidentEventLink> linkCaptor = ArgumentCaptor.forClass(IncidentEventLink.class);
    verify(eventLinkRepository).save(linkCaptor.capture());

    assertThat(linkCaptor.getValue().getIncident()).isSameAs(created);
    assertThat(linkCaptor.getValue().getEvent()).isSameAs(event);

    ArgumentCaptor<CorrelationDecision> decisionCaptor =
        ArgumentCaptor.forClass(CorrelationDecision.class);
    verify(decisionRecorder).record(eq(organisationId), eq(projectId), decisionCaptor.capture());

    CorrelationDecision decision = decisionCaptor.getValue();

    assertThat(decision.eligible()).isTrue();
    assertThat(decision.selectedIncidentId()).isEqualTo(created.getId());
    assertThat(decision.score()).isZero();
    assertThat(decision.threshold()).isEqualTo(5);
    assertThat(decision.matchedDimensions()).isEmpty();
    assertThat(decision.consideredCandidates()).isEmpty();
  }

  @Test
  void reusesMatchingIncidentForRelatedFailure() {
    IncidentRepository incidentRepository = mock(IncidentRepository.class);
    IncidentEventLinkRepository eventLinkRepository = mock(IncidentEventLinkRepository.class);
    IncidentCorrelationDecisionRecorder decisionRecorder =
        mock(IncidentCorrelationDecisionRecorder.class);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID incomingEventId = UUID.randomUUID();
    UUID previousEventId = UUID.randomUUID();
    UUID existingIncidentId = UUID.randomUUID();

    Organisation organisation = organisation(organisationId);
    Project project = project(projectId);

    Incident existingIncident =
        new Incident(
            existingIncidentId,
            organisation,
            project,
            "Existing pipeline incident",
            "Existing incident summary",
            Instant.parse("2026-09-05T09:55:00Z"));

    NormalisedCiEvent previousEvent =
        event(
            previousEventId,
            organisation,
            project,
            PipelineRunStatus.FAILED,
            "run-related",
            Instant.parse("2026-09-05T09:55:00Z"));

    NormalisedCiEvent incomingEvent =
        event(
            incomingEventId,
            organisation,
            project,
            PipelineRunStatus.FAILED,
            "run-related",
            Instant.parse("2026-09-05T10:00:00Z"));

    IncidentEventLink previousLink = mock(IncidentEventLink.class);
    when(previousLink.getEvent()).thenReturn(previousEvent);

    when(eventLinkRepository.findByEventId(incomingEventId)).thenReturn(Optional.empty());
    when(incidentRepository.findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(
            organisationId, projectId))
        .thenReturn(List.of(existingIncident));
    when(eventLinkRepository.findFirstByIncident_IdOrderByLinkedAtAsc(existingIncidentId))
        .thenReturn(Optional.of(previousLink));

    Optional<Incident> result =
        workflow(incidentRepository, eventLinkRepository, decisionRecorder)
            .correlate(incomingEvent);

    assertThat(result).contains(existingIncident);

    verify(incidentRepository, never()).save(any(Incident.class));

    ArgumentCaptor<CorrelationDecision> decisionCaptor =
        ArgumentCaptor.forClass(CorrelationDecision.class);
    verify(decisionRecorder).record(eq(organisationId), eq(projectId), decisionCaptor.capture());

    CorrelationDecision decision = decisionCaptor.getValue();

    assertThat(decision.selectedIncidentId()).isEqualTo(existingIncidentId);
    assertThat(decision.score()).isEqualTo(9);
    assertThat(decision.consideredCandidates()).containsExactly(existingIncidentId);
    assertThat(decision.matchedDimensions())
        .containsExactlyInAnyOrder(
            CorrelationDimension.COMMIT,
            CorrelationDimension.ENVIRONMENT,
            CorrelationDimension.EVENT_FAMILY,
            CorrelationDimension.PIPELINE_RUN,
            CorrelationDimension.TIME_WINDOW);

    ArgumentCaptor<IncidentEventLink> linkCaptor = ArgumentCaptor.forClass(IncidentEventLink.class);
    verify(eventLinkRepository).save(linkCaptor.capture());

    assertThat(linkCaptor.getValue().getIncident()).isSameAs(existingIncident);
    assertThat(linkCaptor.getValue().getEvent()).isSameAs(incomingEvent);
  }

  @Test
  void ignoresIncidentWithoutAnchorEventAndCreatesNewIncident() {
    IncidentRepository incidentRepository = mock(IncidentRepository.class);
    IncidentEventLinkRepository eventLinkRepository = mock(IncidentEventLinkRepository.class);
    IncidentCorrelationDecisionRecorder decisionRecorder =
        mock(IncidentCorrelationDecisionRecorder.class);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID incomingEventId = UUID.randomUUID();
    UUID existingIncidentId = UUID.randomUUID();

    Organisation organisation = organisation(organisationId);
    Project project = project(projectId);

    Incident existingIncident =
        new Incident(
            existingIncidentId,
            organisation,
            project,
            "Unanchored incident",
            "No event link exists.",
            Instant.parse("2026-09-05T09:55:00Z"));

    NormalisedCiEvent incomingEvent =
        event(
            incomingEventId,
            organisation,
            project,
            PipelineRunStatus.FAILED,
            "run-new",
            Instant.parse("2026-09-05T10:00:00Z"));

    when(eventLinkRepository.findByEventId(incomingEventId)).thenReturn(Optional.empty());
    when(incidentRepository.findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(
            organisationId, projectId))
        .thenReturn(List.of(existingIncident));
    when(eventLinkRepository.findFirstByIncident_IdOrderByLinkedAtAsc(existingIncidentId))
        .thenReturn(Optional.empty());

    Optional<Incident> result =
        workflow(incidentRepository, eventLinkRepository, decisionRecorder)
            .correlate(incomingEvent);

    ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
    verify(incidentRepository).save(incidentCaptor.capture());

    assertThat(result).contains(incidentCaptor.getValue());
    assertThat(incidentCaptor.getValue()).isNotSameAs(existingIncident);
  }

  private static IncidentCorrelationWorkflow workflow(
      IncidentRepository incidentRepository,
      IncidentEventLinkRepository eventLinkRepository,
      IncidentCorrelationDecisionRecorder decisionRecorder) {
    return new IncidentCorrelationWorkflow(
        incidentRepository, eventLinkRepository, new IncidentCorrelationEngine(), decisionRecorder);
  }

  private static Organisation organisation(UUID id) {
    Organisation organisation = mock(Organisation.class);
    when(organisation.getId()).thenReturn(id);
    return organisation;
  }

  private static Project project(UUID id) {
    Project project = mock(Project.class);
    when(project.getId()).thenReturn(id);
    return project;
  }

  private static NormalisedCiEvent event(
      UUID id,
      Organisation organisation,
      Project project,
      PipelineRunStatus status,
      String externalRunId,
      Instant occurredAt) {
    NormalisedCiEvent event = mock(NormalisedCiEvent.class);

    when(event.getId()).thenReturn(id);
    when(event.getOrganisation()).thenReturn(organisation);
    when(event.getProject()).thenReturn(project);
    when(event.getEventType()).thenReturn(NormalisedEventType.PIPELINE_RUN_COMPLETED);
    when(event.getPipelineStatus()).thenReturn(status);
    when(event.getCommitSha()).thenReturn("abc123");
    when(event.getEnvironmentName()).thenReturn("production");
    when(event.getExternalRunId()).thenReturn(externalRunId);
    when(event.getRunAttempt()).thenReturn(1);
    when(event.getOccurredAt()).thenReturn(occurredAt);
    when(event.getReceivedAt()).thenReturn(occurredAt.plusSeconds(1));
    when(event.getPipelineName()).thenReturn("portfolio-build");
    when(event.getEvidenceSummary()).thenReturn("Pipeline failed during verification.");

    return event;
  }
}
