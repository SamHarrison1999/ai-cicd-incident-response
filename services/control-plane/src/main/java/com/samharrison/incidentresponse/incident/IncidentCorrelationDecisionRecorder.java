package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentCorrelationDecisionRecorder {

  private final IncidentCorrelationDecisionRecordRepository repository;
  private final AuditRecorder auditRecorder;

  public IncidentCorrelationDecisionRecorder(
      IncidentCorrelationDecisionRecordRepository repository, AuditRecorder auditRecorder) {
    this.repository = repository;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public IncidentCorrelationDecisionRecord record(
      UUID organisationId, UUID projectId, CorrelationDecision decision) {
    return repository
        .findByEventId(decision.eventId())
        .orElseGet(
            () -> {
              IncidentCorrelationDecisionRecord saved =
                  repository.save(
                      new IncidentCorrelationDecisionRecord(
                          UUID.randomUUID(),
                          organisationId,
                          projectId,
                          decision,
                          dimensionsJson(decision),
                          candidatesJson(decision),
                          Instant.now()));
              auditRecorder.record(
                  null,
                  organisationId,
                  "INCIDENT_CORRELATION_DECISION",
                  "INCIDENT",
                  decision.selectedIncidentId(),
                  "{\"policyVersion\":\""
                      + decision.policyVersion()
                      + "\",\"score\":"
                      + decision.score()
                      + ",\"threshold\":"
                      + decision.threshold()
                      + "}");
              return saved;
            });
  }

  private static String dimensionsJson(CorrelationDecision decision) {
    String values =
        decision.matchedDimensions().stream()
            .map(Enum::name)
            .sorted()
            .map(value -> "\"" + value + "\"")
            .collect(Collectors.joining(","));
    return "[" + values + "]";
  }

  private static String candidatesJson(CorrelationDecision decision) {
    String values =
        decision.consideredCandidates().stream()
            .map(UUID::toString)
            .sorted()
            .map(value -> "\"" + value + "\"")
            .collect(Collectors.joining(","));
    return "[" + values + "]";
  }
}
