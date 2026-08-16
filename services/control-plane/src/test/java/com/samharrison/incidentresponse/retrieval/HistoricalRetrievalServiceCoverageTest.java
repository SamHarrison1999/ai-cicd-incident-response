package com.samharrison.incidentresponse.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class HistoricalRetrievalServiceCoverageTest {

  @Mock private HistoricalRetrievalQueryService queryService;
  @Mock private ProjectRepository projectRepository;
  @Mock private TenantAccessService tenantAccessService;

  private HistoricalRetrievalService service;
  private UUID userId;
  private UUID organisationId;
  private UUID projectId;

  @BeforeEach
  void setUp() {
    service = new HistoricalRetrievalService(queryService, projectRepository, tenantAccessService);
    userId = UUID.randomUUID();
    organisationId = UUID.randomUUID();
    projectId = UUID.randomUUID();
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(mock(Project.class)));
  }

  @Test
  void mapsItemsAndBuildsNextCursorForANextPage() {
    UUID recordId = UUID.randomUUID();
    Instant occurredAt = Instant.parse("2026-08-16T10:00:00Z");
    HistoricalRetrievalRecord record = mock(HistoricalRetrievalRecord.class);
    when(record.getId()).thenReturn(recordId);
    when(record.getIncidentId()).thenReturn(UUID.randomUUID());
    when(record.getSourceKind()).thenReturn(HistoricalSourceKind.EVIDENCE);
    when(record.getSourceId()).thenReturn(UUID.randomUUID());
    when(record.getOccurredAt()).thenReturn(occurredAt);
    when(record.getProvider()).thenReturn("github");
    when(record.getPipelineName()).thenReturn("build");
    when(record.getEnvironmentName()).thenReturn("production");
    when(record.getGitRef()).thenReturn("main");
    when(record.getCommitSha()).thenReturn("abc123");
    when(record.getDiagnosisCategory()).thenReturn("DEPENDENCY_FAILURE");
    when(record.getSummary()).thenReturn("Dependency unavailable");
    when(record.getMatchExplanation()).thenReturn("Same provider and project");
    when(record.getProvenanceReference()).thenReturn("evidence:e-1");
    when(queryService.search(eq(organisationId), eq(projectId), any(), eq(null), eq(null)))
        .thenReturn(
            new SliceImpl<>(
                List.of(record), org.springframework.data.domain.PageRequest.of(0, 10), true));

    HistoricalRetrievalService.HistoricalRetrievalPage page =
        service.search(
            userId,
            organisationId,
            projectId,
            " DEPENDENCY_FAILURE ",
            " github ",
            " build ",
            " production ",
            " main ",
            " abc123 ",
            "2026-08-15T00:00:00Z",
            "2026-08-17T00:00:00Z",
            " timeout ",
            null,
            10);

    assertThat(page.items()).hasSize(1);
    assertThat(page.items().getFirst().sourceKind()).isEqualTo("EVIDENCE");
    assertThat(page.nextCursor()).isNotBlank();
    assertThat(page.hasNext()).isTrue();
  }

  @Test
  void rejectsMissingProject() {
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.search(
                    userId,
                    organisationId,
                    projectId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    10))
        .isInstanceOf(com.samharrison.incidentresponse.tenancy.TenantAccessException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void rejectsMalformedFiltersAndCursor() {
    assertThatThrownBy(
            () ->
                service.search(
                    userId,
                    organisationId,
                    projectId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "not-an-instant",
                    null,
                    null,
                    null,
                    10))
        .isInstanceOf(com.samharrison.incidentresponse.tenancy.TenantAccessException.class);

    assertThatThrownBy(
            () ->
                service.search(
                    userId,
                    organisationId,
                    projectId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "invalid-cursor",
                    10))
        .isInstanceOf(com.samharrison.incidentresponse.tenancy.TenantAccessException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void returnsAnEmptyPageWithoutBuildingACursor() {
    when(queryService.search(eq(organisationId), eq(projectId), any(), eq(null), eq(null)))
        .thenReturn(
            new SliceImpl<>(
                List.of(), org.springframework.data.domain.PageRequest.of(0, 10), true));

    HistoricalRetrievalService.HistoricalRetrievalPage page =
        service.search(
            userId,
            organisationId,
            projectId,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            10);

    assertThat(page.items()).isEmpty();
    assertThat(page.nextCursor()).isNull();
    assertThat(page.hasNext()).isTrue();
  }

  @Test
  void decodesCursorAndPassesItsPositionToTheQuery() {
    UUID recordId = UUID.randomUUID();
    Instant occurredAt = Instant.parse("2026-08-16T10:00:00Z");
    HistoricalRetrievalCursor cursor = new HistoricalRetrievalCursor(occurredAt, recordId);
    when(queryService.search(
            eq(organisationId), eq(projectId), any(), eq(occurredAt), eq(recordId)))
        .thenReturn(
            new SliceImpl<>(
                List.of(), org.springframework.data.domain.PageRequest.of(0, 10), false));

    service.search(
        userId,
        organisationId,
        projectId,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        cursor.encode(),
        10);
  }
}
