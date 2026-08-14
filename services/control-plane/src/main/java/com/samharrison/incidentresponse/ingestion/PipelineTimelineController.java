package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/timeline")
public class PipelineTimelineController {
  private final PipelineTimelineService service;
  private final CurrentUserProvider currentUserProvider;

  public PipelineTimelineController(
      PipelineTimelineService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  PipelineTimelineResponse list(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String branch,
      @RequestParam(required = false) String commitSha,
      @RequestParam(required = false) String environment,
      @RequestParam(required = false) String eventType,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "25") int limit) {
    PipelineTimelineService.TimelinePage page =
        service.list(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            status,
            branch,
            commitSha,
            environment,
            eventType,
            from,
            to,
            cursor,
            limit);
    return new PipelineTimelineResponse(
        page.items().stream().map(PipelineTimelineController::toResponse).toList(),
        page.nextCursor(),
        page.hasNext());
  }

  private static TimelineEventResponse toResponse(PipelineTimelineService.TimelineEvent event) {
    return new TimelineEventResponse(
        event.id(),
        event.pipelineRunId(),
        event.provider(),
        event.eventType(),
        event.status(),
        event.externalRunId(),
        event.pipelineName(),
        event.attempt(),
        event.commitSha(),
        event.gitRef(),
        event.environmentName(),
        event.occurredAt(),
        event.receivedAt(),
        event.evidenceSummary());
  }

  public record PipelineTimelineResponse(
      List<TimelineEventResponse> items, String nextCursor, boolean hasNext) {}

  public record TimelineEventResponse(
      UUID id,
      UUID pipelineRunId,
      String provider,
      String eventType,
      String status,
      String externalRunId,
      String pipelineName,
      int attempt,
      String commitSha,
      String gitRef,
      String environmentName,
      Instant occurredAt,
      Instant receivedAt,
      String evidenceSummary) {}
}
