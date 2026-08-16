package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IncidentServiceCoverageTest {

  @Test
  void transitionsAnIncidentAndRecordsAudit() {
    IncidentRepository repository = mock(IncidentRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    AuditRecorder audit = mock(AuditRecorder.class);
    IncidentService service = new IncidentService(repository, tenant, audit);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID incidentId = UUID.randomUUID();
    Incident incident = mock(Incident.class);
    when(repository.findByIdAndOrganisationIdAndProjectId(incidentId, organisationId, projectId))
        .thenReturn(Optional.of(incident));

    assertThat(
            service.transition(
                userId,
                organisationId,
                projectId,
                incidentId,
                IncidentStatus.TRIAGED,
                Instant.parse("2026-08-16T10:00:00Z")))
        .isSameAs(incident);
    verify(tenant).requireRole(eq(organisationId), eq(userId), anySet());
    verify(incident).transitionTo(any(), any());
    verify(audit)
        .record(
            userId,
            organisationId,
            "INCIDENT_STATUS_CHANGED",
            "INCIDENT",
            incidentId,
            "{\"status\":\"TRIAGED\"}");
  }

  @Test
  void hidesMissingIncident() {
    IncidentRepository repository = mock(IncidentRepository.class);
    when(repository.findByIdAndOrganisationIdAndProjectId(any(), any(), any()))
        .thenReturn(Optional.empty());
    IncidentService service =
        new IncidentService(repository, mock(TenantAccessService.class), mock(AuditRecorder.class));

    assertThatThrownBy(
            () ->
                service.get(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The incident was not found.");
  }
}
