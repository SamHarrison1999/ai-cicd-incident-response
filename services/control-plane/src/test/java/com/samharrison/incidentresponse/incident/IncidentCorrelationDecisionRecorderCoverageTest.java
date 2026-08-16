package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IncidentCorrelationDecisionRecorderCoverageTest {
  @Test
  void recordsNewDecisionsWithSortedJsonAndReusesExistingRecords() {
    IncidentCorrelationDecisionRecordRepository repository =
        mock(IncidentCorrelationDecisionRecordRepository.class);
    AuditRecorder auditRecorder = mock(AuditRecorder.class);
    UUID eventId = UUID.randomUUID();
    UUID incidentId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    CorrelationDecision decision =
        new CorrelationDecision(
            eventId,
            incidentId,
            "policy-v1",
            9,
            5,
            Set.of(CorrelationDimension.COMMIT, CorrelationDimension.ENVIRONMENT),
            List.of(UUID.randomUUID(), UUID.randomUUID()),
            true);
    IncidentCorrelationDecisionRecord saved = mock(IncidentCorrelationDecisionRecord.class);
    when(repository.findByEventId(eventId)).thenReturn(Optional.empty(), Optional.of(saved));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    IncidentCorrelationDecisionRecorder recorder =
        new IncidentCorrelationDecisionRecorder(repository, auditRecorder);
    IncidentCorrelationDecisionRecord created =
        recorder.record(organisationId, projectId, decision);
    IncidentCorrelationDecisionRecord reused = recorder.record(organisationId, projectId, decision);

    assertThat(created.getMatchedDimensions()).contains("COMMIT", "ENVIRONMENT");
    assertThat(created.getConsideredCandidates()).startsWith("[").endsWith("]");
    assertThat(reused).isSameAs(saved);
    verify(auditRecorder)
        .record(
            null,
            organisationId,
            "INCIDENT_CORRELATION_DECISION",
            "INCIDENT",
            incidentId,
            "{\"policyVersion\":\"policy-v1\",\"score\":9,\"threshold\":5}");
  }
}
