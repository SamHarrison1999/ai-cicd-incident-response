package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.project.ProjectStatus;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceServiceTest {
  @Test
  void requiresTenantAccessAndStoresOnlyRedactedContent() {
    EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    ProjectRepository projectRepository = mock(ProjectRepository.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    AuditRecorder auditRecorder = mock(AuditRecorder.class);
    EvidenceService service =
        new EvidenceService(
            evidenceRepository, projectRepository, tenantAccessService, auditRecorder);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Organisation organisation =
        new Organisation(organisationId, "Test Organisation", "evidence-test");
    Project project =
        new Project(
            projectId,
            organisation,
            "Evidence Project",
            "evidence",
            "Test project",
            ProjectStatus.ACTIVE);
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(java.util.Optional.of(project));
    when(evidenceRepository.save(any(Evidence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Evidence result =
        service.create(
            userId,
            organisationId,
            projectId,
            EvidenceKind.LOG_EXCERPT,
            RetentionClass.STANDARD,
            "synthetic",
            "run-1",
            Instant.parse("2026-08-14T12:00:00Z"),
            "secret=hidden");

    assertThat(result.getContent()).doesNotContain("hidden");
    assertThat(result.getContentHash()).hasSize(64);
    verify(tenantAccessService).requireActiveMembership(organisationId, userId);
    verify(auditRecorder).record(any(), any(), any(), any(), any(), any());
  }
}
