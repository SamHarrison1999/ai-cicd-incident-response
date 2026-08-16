package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.incident.EvidenceEventLinkRepository;
import com.samharrison.incidentresponse.incident.IncidentEvidenceLinkRepository;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceMissingCoverageTest {

  @Test
  void coversEvidenceBoundsAndMissingTenantResources() {
    assertThatThrownBy(
            () ->
                new Evidence(
                    UUID.randomUUID(),
                    mock(com.samharrison.incidentresponse.organisation.Organisation.class),
                    mock(com.samharrison.incidentresponse.project.Project.class),
                    EvidenceKind.LOG_EXCERPT,
                    RetentionClass.STANDARD,
                    "github",
                    "run-1",
                    Instant.now(),
                    Instant.now(),
                    "a".repeat(64),
                    "x".repeat(EvidenceRedactor.MAX_CHARS + 1),
                    1))
        .isInstanceOf(IllegalArgumentException.class);

    org.assertj.core.api.Assertions.assertThat(
            EvidenceSanitiser.sanitise("x".repeat(EvidenceRedactor.MAX_CHARS + 1)).warnings())
        .contains(SanitisationWarning.CONTENT_BOUNDED);

    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    ProjectRepository projects = mock(ProjectRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    when(projects.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.empty());

    EvidenceService evidenceService =
        new EvidenceService(
            mock(EvidenceRepository.class),
            projects,
            tenant,
            mock(com.samharrison.incidentresponse.audit.AuditRecorder.class));
    assertThatThrownBy(() -> evidenceService.list(userId, organisationId, projectId))
        .isInstanceOf(com.samharrison.incidentresponse.tenancy.TenantAccessException.class);

    EvidenceSearchService searchService =
        new EvidenceSearchService(mock(EvidenceRepository.class), projects, tenant);
    assertThatThrownBy(
            () ->
                searchService.search(
                    userId,
                    organisationId,
                    projectId,
                    new EvidenceSearchCriteria(null, null, null, null, null, 20),
                    null))
        .isInstanceOf(com.samharrison.incidentresponse.tenancy.TenantAccessException.class);

    EvidenceViewerService viewerService =
        new EvidenceViewerService(
            mock(EvidenceRepository.class),
            projects,
            mock(IncidentEvidenceLinkRepository.class),
            mock(EvidenceEventLinkRepository.class),
            tenant);
    assertThatThrownBy(
            () -> viewerService.get(userId, organisationId, projectId, UUID.randomUUID()))
        .isInstanceOf(com.samharrison.incidentresponse.tenancy.TenantAccessException.class);
  }

  @Test
  void rejectsMissingEvidenceAfterProjectValidation() {
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    ProjectRepository projects = mock(ProjectRepository.class);
    EvidenceRepository evidence = mock(EvidenceRepository.class);
    when(projects.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(mock(com.samharrison.incidentresponse.project.Project.class)));
    when(evidence.findByIdAndOrganisationIdAndProjectId(evidenceId, organisationId, projectId))
        .thenReturn(Optional.empty());

    EvidenceViewerService viewerService =
        new EvidenceViewerService(
            evidence,
            projects,
            mock(IncidentEvidenceLinkRepository.class),
            mock(EvidenceEventLinkRepository.class),
            mock(TenantAccessService.class));
    assertThatThrownBy(() -> viewerService.get(userId, organisationId, projectId, evidenceId))
        .isInstanceOf(com.samharrison.incidentresponse.tenancy.TenantAccessException.class);
  }
}
