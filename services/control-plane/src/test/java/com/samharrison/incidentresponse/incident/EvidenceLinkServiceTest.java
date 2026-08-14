package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.evidence.EvidenceRepository;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEventRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceLinkServiceTest {

  @Test
  void missingTenantOwnedEvidenceStopsLinkResolution() {
    EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    IncidentRepository incidentRepository = mock(IncidentRepository.class);
    NormalisedCiEventRepository eventRepository = mock(NormalisedCiEventRepository.class);
    IncidentEvidenceLinkRepository incidentLinkRepository =
        mock(IncidentEvidenceLinkRepository.class);
    EvidenceEventLinkRepository eventLinkRepository = mock(EvidenceEventLinkRepository.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    AuditRecorder auditRecorder = mock(AuditRecorder.class);
    EvidenceLinkService service =
        new EvidenceLinkService(
            evidenceRepository,
            incidentRepository,
            eventRepository,
            incidentLinkRepository,
            eventLinkRepository,
            tenantAccessService,
            auditRecorder);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();

    when(evidenceRepository.findByIdAndOrganisationIdAndProjectId(
            evidenceId, organisationId, projectId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.linkToIncident(
                    userId, organisationId, projectId, evidenceId, UUID.randomUUID()))
        .isInstanceOf(TenantAccessException.class);

    verify(tenantAccessService).requireRole(eq(organisationId), eq(userId), anySet());
  }
}
