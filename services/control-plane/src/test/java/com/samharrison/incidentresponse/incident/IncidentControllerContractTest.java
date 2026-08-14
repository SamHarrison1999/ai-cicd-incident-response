package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectStatus;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IncidentControllerContractTest {

  @Test
  void responseContainsOnlyBoundedIncidentFields() {
    Incident incident = newIncident();
    IncidentController.IncidentResponse response =
        new IncidentController.IncidentResponse(
            incident.getId(),
            incident.getStatus().name(),
            incident.getTitle(),
            incident.getSummary(),
            incident.getDetectedAt(),
            incident.getResolvedAt(),
            incident.getCreatedAt(),
            incident.getUpdatedAt());

    assertThat(response.id()).isEqualTo(incident.getId());
    assertThat(response.status()).isEqualTo("DETECTED");
    assertThat(response.summary()).doesNotContain("payload", "signature", "secret");
  }

  @Test
  void serviceRequiresTenantAccessBeforeListing() {
    IncidentRepository repository = mock(IncidentRepository.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    AuditRecorder auditRecorder = mock(AuditRecorder.class);
    IncidentService service = new IncidentService(repository, tenantAccessService, auditRecorder);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    when(repository.findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(
            organisationId, projectId))
        .thenReturn(List.of());

    assertThat(service.list(userId, organisationId, projectId)).isEmpty();

    verify(tenantAccessService).requireActiveMembership(organisationId, userId);
    verify(repository)
        .findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(organisationId, projectId);
  }

  private static Incident newIncident() {
    Organisation organisation =
        new Organisation(UUID.randomUUID(), "Test Organisation", "platform");
    Project project =
        new Project(
            UUID.randomUUID(),
            organisation,
            "Test Project",
            "incident-response",
            "Incident controller test project",
            ProjectStatus.ACTIVE);
    return new Incident(
        UUID.randomUUID(),
        organisation,
        project,
        "Pipeline failure",
        "A bounded incident summary.",
        Instant.parse("2026-08-14T12:00:00Z"));
  }
}
