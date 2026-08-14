package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.incident.EvidenceEventLinkRepository;
import com.samharrison.incidentresponse.incident.IncidentEvidenceLinkRepository;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.project.ProjectStatus;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceViewerServiceTest {

  @Test
  void viewerRequiresTenantAccessAndReturnsOnlyPersistedRedactedContent() {
    EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    ProjectRepository projectRepository = mock(ProjectRepository.class);
    IncidentEvidenceLinkRepository incidentLinkRepository =
        mock(IncidentEvidenceLinkRepository.class);
    EvidenceEventLinkRepository eventLinkRepository = mock(EvidenceEventLinkRepository.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    EvidenceViewerService service =
        new EvidenceViewerService(
            evidenceRepository,
            projectRepository,
            incidentLinkRepository,
            eventLinkRepository,
            tenantAccessService);
    Organisation organisation =
        new Organisation(UUID.randomUUID(), "Test Organisation", "viewer-test");
    Project project =
        new Project(
            UUID.randomUUID(),
            organisation,
            "Test Project",
            "viewer-test",
            "Evidence viewer test project",
            ProjectStatus.ACTIVE);
    UUID userId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    UUID organisationId = organisation.getId();
    UUID projectId = project.getId();
    Evidence evidence =
        new Evidence(
            evidenceId,
            organisation,
            project,
            EvidenceKind.LOG_EXCERPT,
            RetentionClass.STANDARD,
            "github",
            "delivery-1",
            Instant.parse("2026-08-14T12:00:00Z"),
            Instant.parse("2026-08-14T12:01:00Z"),
            "a".repeat(64),
            "token=[REDACTED]",
            1);
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(project));
    when(evidenceRepository.findByIdAndOrganisationIdAndProjectId(
            evidenceId, organisationId, projectId))
        .thenReturn(Optional.of(evidence));
    when(incidentLinkRepository
            .findAllByEvidenceIdAndOrganisationIdAndProjectIdOrderByLinkedAtAscIdAsc(
                evidenceId, organisationId, projectId))
        .thenReturn(List.of());
    when(eventLinkRepository
            .findAllByEvidenceIdAndOrganisationIdAndProjectIdOrderByLinkedAtAscIdAsc(
                evidenceId, organisationId, projectId))
        .thenReturn(List.of());

    EvidenceViewerService.EvidenceView view =
        service.get(userId, organisationId, projectId, evidenceId);

    assertThat(view.evidence().getContent()).isEqualTo("token=[REDACTED]");
    assertThat(view.incidentIds()).isEmpty();
    assertThat(view.eventIds()).isEmpty();
    verify(tenantAccessService).requireActiveMembership(organisationId, userId);
  }
}
