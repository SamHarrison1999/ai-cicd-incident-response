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
import com.samharrison.incidentresponse.evidence.Evidence;
import com.samharrison.incidentresponse.evidence.EvidenceRepository;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEventRepository;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceLinkServiceCoverageTest {

  @Test
  void linksEvidenceToIncidentAndEventAndRecordsAudit() {
    EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    IncidentRepository incidentRepository = mock(IncidentRepository.class);
    NormalisedCiEventRepository eventRepository = mock(NormalisedCiEventRepository.class);
    IncidentEvidenceLinkRepository incidentLinks = mock(IncidentEvidenceLinkRepository.class);
    EvidenceEventLinkRepository eventLinks = mock(EvidenceEventLinkRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    AuditRecorder audit = mock(AuditRecorder.class);
    EvidenceLinkService service =
        new EvidenceLinkService(
            evidenceRepository,
            incidentRepository,
            eventRepository,
            incidentLinks,
            eventLinks,
            tenant,
            audit);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    UUID incidentId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();
    Evidence evidence = mock(Evidence.class);
    Incident incident = mock(Incident.class);
    NormalisedCiEvent event = mock(NormalisedCiEvent.class);
    Organisation organisation = mock(Organisation.class);
    Project project = mock(Project.class);
    when(organisation.getId()).thenReturn(organisationId);
    when(project.getId()).thenReturn(projectId);
    when(evidence.getOrganisation()).thenReturn(organisation);
    when(evidence.getProject()).thenReturn(project);
    when(incident.getOrganisation()).thenReturn(organisation);
    when(incident.getProject()).thenReturn(project);
    when(evidenceRepository.findByIdAndOrganisationIdAndProjectId(
            evidenceId, organisationId, projectId))
        .thenReturn(Optional.of(evidence));
    when(incidentRepository.findByIdAndOrganisationIdAndProjectId(
            incidentId, organisationId, projectId))
        .thenReturn(Optional.of(incident));
    when(eventRepository.findByIdAndOrganisationIdAndProjectId(eventId, organisationId, projectId))
        .thenReturn(Optional.of(event));
    when(incidentLinks.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(eventLinks.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    assertThat(service.linkToIncident(userId, organisationId, projectId, evidenceId, incidentId))
        .isNotNull();
    assertThat(service.linkToEvent(userId, organisationId, projectId, evidenceId, eventId))
        .isNotNull();
    verify(tenant, org.mockito.Mockito.times(2))
        .requireRole(eq(organisationId), eq(userId), anySet());
  }

  @Test
  void rejectsMissingIncidentAndEventWithoutWritingLinks() {
    EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    IncidentRepository incidentRepository = mock(IncidentRepository.class);
    NormalisedCiEventRepository eventRepository = mock(NormalisedCiEventRepository.class);
    IncidentEvidenceLinkRepository incidentLinks = mock(IncidentEvidenceLinkRepository.class);
    EvidenceEventLinkRepository eventLinks = mock(EvidenceEventLinkRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    EvidenceLinkService service =
        new EvidenceLinkService(
            evidenceRepository,
            incidentRepository,
            eventRepository,
            incidentLinks,
            eventLinks,
            tenant,
            mock(AuditRecorder.class));
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    when(evidenceRepository.findByIdAndOrganisationIdAndProjectId(
            evidenceId, organisationId, projectId))
        .thenReturn(Optional.of(mock(Evidence.class)));
    when(incidentRepository.findByIdAndOrganisationIdAndProjectId(
            any(), eq(organisationId), eq(projectId)))
        .thenReturn(Optional.empty());
    when(eventRepository.findByIdAndOrganisationIdAndProjectId(
            any(), eq(organisationId), eq(projectId)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.linkToIncident(
                    UUID.randomUUID(), organisationId, projectId, evidenceId, UUID.randomUUID()))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The incident was not found.");
    assertThatThrownBy(
            () ->
                service.linkToEvent(
                    UUID.randomUUID(), organisationId, projectId, evidenceId, UUID.randomUUID()))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The event was not found.");
  }
}
