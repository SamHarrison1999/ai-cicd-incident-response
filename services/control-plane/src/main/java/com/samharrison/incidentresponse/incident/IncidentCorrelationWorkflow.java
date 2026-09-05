package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentCorrelationWorkflow {

  private final IncidentRepository incidentRepository;
  private final IncidentEventLinkRepository eventLinkRepository;
  private final IncidentCorrelationEngine correlationEngine;
  private final IncidentCorrelationDecisionRecorder decisionRecorder;

  public IncidentCorrelationWorkflow(
      IncidentRepository incidentRepository,
      IncidentEventLinkRepository eventLinkRepository,
      IncidentCorrelationEngine correlationEngine,
      IncidentCorrelationDecisionRecorder decisionRecorder) {
    this.incidentRepository = incidentRepository;
    this.eventLinkRepository = eventLinkRepository;
    this.correlationEngine = correlationEngine;
    this.decisionRecorder = decisionRecorder;
  }

  @Transactional
  public Optional<Incident> correlate(NormalisedCiEvent event) {
    Optional<IncidentEventLink> existingLink = eventLinkRepository.findByEventId(event.getId());
    if (existingLink.isPresent()) {
      return Optional.of(existingLink.get().getIncident());
    }

    UUID organisationId = event.getOrganisation().getId();
    UUID projectId = event.getProject().getId();

    List<Incident> incidents =
        incidentRepository.findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(
            organisationId, projectId);

    List<IncidentCorrelationCandidate> candidates =
        incidents.stream().map(this::candidateFor).flatMap(Optional::stream).toList();

    CorrelationDecision initialDecision =
        correlationEngine.evaluate(toCorrelationEvent(event), candidates);

    if (!initialDecision.eligible()) {
      decisionRecorder.record(organisationId, projectId, initialDecision);
      return Optional.empty();
    }

    Map<UUID, Incident> incidentsById =
        incidents.stream().collect(Collectors.toMap(Incident::getId, Function.identity()));

    Incident incident =
        initialDecision.selectedIncidentId() == null
            ? createIncident(event)
            : Objects.requireNonNull(incidentsById.get(initialDecision.selectedIncidentId()));

    eventLinkRepository.save(
        new IncidentEventLink(UUID.randomUUID(), incident, event, event.getReceivedAt()));

    CorrelationDecision recordedDecision =
        new CorrelationDecision(
            initialDecision.eventId(),
            incident.getId(),
            initialDecision.policyVersion(),
            initialDecision.score(),
            initialDecision.threshold(),
            initialDecision.matchedDimensions(),
            initialDecision.consideredCandidates(),
            initialDecision.eligible());

    decisionRecorder.record(organisationId, projectId, recordedDecision);

    return Optional.of(incident);
  }

  private Optional<IncidentCorrelationCandidate> candidateFor(Incident incident) {
    return eventLinkRepository
        .findFirstByIncident_IdOrderByLinkedAtAsc(incident.getId())
        .map(
            link -> {
              NormalisedCiEvent event = link.getEvent();
              return new IncidentCorrelationCandidate(
                  incident.getId(),
                  incident.getOrganisation().getId(),
                  incident.getProject().getId(),
                  incident.getStatus(),
                  incident.getDetectedAt(),
                  event.getEventType(),
                  event.getCommitSha(),
                  event.getEnvironmentName(),
                  event.getExternalRunId(),
                  event.getRunAttempt());
            });
  }

  private CorrelationEvent toCorrelationEvent(NormalisedCiEvent event) {
    return new CorrelationEvent(
        event.getId(),
        event.getOrganisation().getId(),
        event.getProject().getId(),
        event.getOccurredAt(),
        event.getEventType(),
        event.getPipelineStatus(),
        event.getCommitSha(),
        event.getEnvironmentName(),
        event.getExternalRunId(),
        event.getRunAttempt());
  }

  private Incident createIncident(NormalisedCiEvent event) {
    Incident incident =
        new Incident(
            UUID.randomUUID(),
            event.getOrganisation(),
            event.getProject(),
            event.getPipelineName(),
            event.getEvidenceSummary(),
            event.getOccurredAt());

    incidentRepository.save(incident);

    return incident;
  }
}
