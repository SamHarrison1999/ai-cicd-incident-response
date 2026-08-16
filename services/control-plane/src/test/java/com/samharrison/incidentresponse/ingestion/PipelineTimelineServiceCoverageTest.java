package com.samharrison.incidentresponse.ingestion;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class PipelineTimelineServiceCoverageTest {

  @Mock private NormalisedCiEventRepository repository;
  @Mock private ProjectRepository projectRepository;
  @Mock private TenantAccessService tenantAccessService;

  private PipelineTimelineService service;
  private UUID userId;
  private UUID organisationId;
  private UUID projectId;

  @BeforeEach
  void setUp() {
    service = new PipelineTimelineService(repository, projectRepository, tenantAccessService);
    userId = UUID.randomUUID();
    organisationId = UUID.randomUUID();
    projectId = UUID.randomUUID();
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(mock(Project.class)));
  }

  @Test
  void mapsTimelineEventsAndBuildsCursor() {
    UUID eventId = UUID.randomUUID();
    Instant occurredAt = Instant.parse("2026-08-16T10:00:00Z");
    Instant receivedAt = Instant.parse("2026-08-16T10:01:00Z");
    NormalisedCiEvent event = mock(NormalisedCiEvent.class);
    PipelineRun run = mock(PipelineRun.class);
    when(event.getId()).thenReturn(eventId);
    when(event.getPipelineRun()).thenReturn(run);
    when(run.getId()).thenReturn(UUID.randomUUID());
    when(event.getProvider()).thenReturn(EventProvider.GITHUB_ACTIONS);
    when(event.getEventType()).thenReturn(NormalisedEventType.PIPELINE_RUN_COMPLETED);
    when(event.getPipelineStatus()).thenReturn(PipelineRunStatus.FAILED);
    when(event.getExternalRunId()).thenReturn("run-1");
    when(event.getPipelineName()).thenReturn("build");
    when(event.getRunAttempt()).thenReturn(1);
    when(event.getCommitSha()).thenReturn("abc123");
    when(event.getGitRef()).thenReturn("main");
    when(event.getEnvironmentName()).thenReturn("production");
    when(event.getOccurredAt()).thenReturn(occurredAt);
    when(event.getReceivedAt()).thenReturn(receivedAt);
    when(event.getEvidenceSummary()).thenReturn("failure");
    when(repository.searchTimeline(
            eq(projectId),
            eq(organisationId),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(PageRequest.class)))
        .thenReturn(new SliceImpl<>(List.of(event), PageRequest.of(0, 10), true));

    PipelineTimelineService.TimelinePage page =
        service.list(
            userId,
            organisationId,
            projectId,
            "failed",
            " main ",
            "abc123",
            "production",
            "pipeline_run_completed",
            "2026-08-15T00:00:00Z",
            "2026-08-17T00:00:00Z",
            null,
            10);

    assertThat(page.items()).hasSize(1);
    assertThat(page.items().getFirst().pipelineRunId()).isNotNull();
    assertThat(page.nextCursor()).isNotBlank();
  }

  @Test
  void rejectsInvalidProjectFiltersAndCursor() {
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                service.list(
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
                    10))
        .isInstanceOf(RuntimeException.class);

    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(mock(Project.class)));
    assertThatThrownBy(
            () ->
                service.list(
                    userId,
                    organisationId,
                    projectId,
                    "unknown",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    10))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(
            () ->
                service.list(
                    userId,
                    organisationId,
                    projectId,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "bad",
                    null,
                    null,
                    10))
        .isInstanceOf(RuntimeException.class);
    assertThatThrownBy(
            () ->
                service.list(
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
                    0))
        .isInstanceOf(RuntimeException.class);
  }
}
