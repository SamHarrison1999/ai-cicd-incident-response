package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/pipeline-runs")
public class PipelineRunController {
  private final PipelineRunService service;
  private final CurrentUserProvider currentUserProvider;

  public PipelineRunController(
      PipelineRunService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  List<PipelineRunResponse> list(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId) {
    return service
        .list(currentUserProvider.requireUserId(authentication), organisationId, projectId)
        .stream()
        .map(PipelineRunController::toResponse)
        .toList();
  }

  @GetMapping("/{pipelineRunId}")
  PipelineRunResponse get(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID pipelineRunId) {
    return toResponse(
        service.get(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            pipelineRunId));
  }

  private static PipelineRunResponse toResponse(PipelineRun run) {
    return new PipelineRunResponse(
        run.getId(),
        run.getEventSource().getId(),
        run.getProvider().name(),
        run.getExternalRunId(),
        run.getName(),
        run.getAttempt(),
        run.getStatus().name(),
        run.getCommitSha(),
        run.getGitRef(),
        run.getEnvironmentName(),
        run.getStartedAt(),
        run.getCompletedAt(),
        run.getLastEventOccurredAt(),
        run.getUpdatedAt());
  }

  public record PipelineRunResponse(
      UUID id,
      UUID eventSourceId,
      String provider,
      String externalRunId,
      String name,
      int attempt,
      String status,
      String commitSha,
      String gitRef,
      String environmentName,
      Instant startedAt,
      Instant completedAt,
      Instant lastEventOccurredAt,
      Instant updatedAt) {}
}
