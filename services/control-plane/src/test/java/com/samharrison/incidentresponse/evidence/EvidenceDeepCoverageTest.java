package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;

class EvidenceDeepCoverageTest {
  @Test
  void redactorCoversSecretBearerSignatureAndBothBounds() {
    String redacted =
        EvidenceRedactor.redact(
                "password=one token:two Bearer abc.def sha256=0123456789abcdef0123456789abcdef")
            .content();
    assertThat(redacted).doesNotContain("one", "two", "abc.def");
    assertThat(redacted).contains("[REDACTED]");
    assertThat(EvidenceRedactor.redact("a\r\nb\rc").lineCount()).isEqualTo(3);
    assertThat(EvidenceRedactor.redact("x\n".repeat(EvidenceRedactor.MAX_LINES + 2)).content())
        .contains("[TRUNCATED]");
    assertThat(EvidenceRedactor.redact("x".repeat(EvidenceRedactor.MAX_CHARS + 10)).content())
        .contains("[TRUNCATED]");
    assertThatThrownBy(() -> EvidenceRedactor.redact(" "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void searchCoversCursorAndSliceBranches() {
    EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    ProjectRepository projectRepository = mock(ProjectRepository.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    Project project = mock(Project.class);
    Evidence first = mock(Evidence.class);
    Evidence second = mock(Evidence.class);
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID firstId = UUID.randomUUID();
    UUID secondId = UUID.randomUUID();
    Instant firstTime = Instant.parse("2026-01-01T00:00:00Z");
    Instant secondTime = Instant.parse("2026-01-02T00:00:00Z");
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(project));
    when(first.getId()).thenReturn(firstId);
    when(first.getOccurredAt()).thenReturn(firstTime);
    when(second.getId()).thenReturn(secondId);
    when(second.getOccurredAt()).thenReturn(secondTime);
    EvidenceSearchService service =
        new EvidenceSearchService(evidenceRepository, projectRepository, tenantAccessService);
    EvidenceSearchCriteria criteria =
        new EvidenceSearchCriteria(EvidenceKind.LOG_EXCERPT, " github ", " query ", null, null, 10);

    when(evidenceRepository.search(
            org.mockito.ArgumentMatchers.eq(organisationId),
            org.mockito.ArgumentMatchers.eq(projectId),
            org.mockito.ArgumentMatchers.eq(EvidenceKind.LOG_EXCERPT),
            org.mockito.ArgumentMatchers.eq("github"),
            org.mockito.ArgumentMatchers.eq("query"),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(
            new SliceImpl<>(
                List.of(first), org.springframework.data.domain.PageRequest.of(0, 10), true));
    assertThat(
            service
                .search(UUID.randomUUID(), organisationId, projectId, criteria, null)
                .nextCursor())
        .isNotNull();

    when(evidenceRepository.search(
            org.mockito.ArgumentMatchers.eq(organisationId),
            org.mockito.ArgumentMatchers.eq(projectId),
            org.mockito.ArgumentMatchers.eq(EvidenceKind.LOG_EXCERPT),
            org.mockito.ArgumentMatchers.eq("github"),
            org.mockito.ArgumentMatchers.eq("query"),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.isNull(),
            org.mockito.ArgumentMatchers.eq(firstTime),
            org.mockito.ArgumentMatchers.eq(firstId),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(
            new SliceImpl<>(
                List.of(second), org.springframework.data.domain.PageRequest.of(1, 10), false));
    assertThat(
            service
                .search(
                    UUID.randomUUID(),
                    organisationId,
                    projectId,
                    criteria,
                    new EvidenceCursor(firstTime, firstId))
                .nextCursor())
        .isNull();

    when(evidenceRepository.search(
            eq(organisationId),
            eq(projectId),
            eq(EvidenceKind.LOG_EXCERPT),
            eq("github"),
            eq("query"),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            any()))
        .thenReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 10), true));
    assertThat(
            service
                .search(UUID.randomUUID(), organisationId, projectId, criteria, null)
                .nextCursor())
        .isNull();
  }

  @Test
  void viewerControllerMapsTheBoundedProjection() {
    EvidenceViewerService viewerService = mock(EvidenceViewerService.class);
    CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    Authentication authentication = mock(Authentication.class);
    Evidence evidence = mock(Evidence.class);
    UUID userId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    when(currentUserProvider.requireUserId(authentication)).thenReturn(userId);
    when(evidence.getId()).thenReturn(evidenceId);
    when(evidence.getKind()).thenReturn(EvidenceKind.LOG_EXCERPT);
    when(evidence.getRetentionClass()).thenReturn(RetentionClass.STANDARD);
    when(evidence.getSourceSystem()).thenReturn("github");
    when(evidence.getSourceReference()).thenReturn("run-1");
    when(evidence.getOccurredAt()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
    when(evidence.getIngestedAt()).thenReturn(Instant.parse("2026-01-01T00:01:00Z"));
    when(evidence.getContentHash()).thenReturn("a".repeat(64));
    when(evidence.getContent()).thenReturn("bounded");
    when(evidence.getContentLineCount()).thenReturn(1);
    when(viewerService.get(userId, UUID.randomUUID(), UUID.randomUUID(), evidenceId))
        .thenReturn(new EvidenceViewerService.EvidenceView(evidence, List.of(), List.of()));
    // The controller contract is covered by the existing tests; this test exercises the mapper
    // through a real controller call with a stable authenticated identity.
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    when(viewerService.get(userId, organisationId, projectId, evidenceId))
        .thenReturn(new EvidenceViewerService.EvidenceView(evidence, List.of(), List.of()));
    assertThat(
            new EvidenceViewerController(viewerService, currentUserProvider)
                .get(authentication, organisationId, projectId, evidenceId)
                .content())
        .isEqualTo("bounded");
  }
}
