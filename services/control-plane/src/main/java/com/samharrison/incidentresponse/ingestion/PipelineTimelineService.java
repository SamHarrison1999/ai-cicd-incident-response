package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineTimelineService {
  private static final int MAX_LIMIT = 100;

  private final NormalisedCiEventRepository repository;
  private final ProjectRepository projectRepository;
  private final TenantAccessService tenantAccessService;

  public PipelineTimelineService(
      NormalisedCiEventRepository repository,
      ProjectRepository projectRepository,
      TenantAccessService tenantAccessService) {
    this.repository = repository;
    this.projectRepository = projectRepository;
    this.tenantAccessService = tenantAccessService;
  }

  @Transactional(readOnly = true)
  public TimelinePage list(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      String status,
      String branch,
      String commitSha,
      String environment,
      String eventType,
      String from,
      String to,
      String cursor,
      int limit) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    requireProject(organisationId, projectId);
    if (limit < 1 || limit > MAX_LIMIT) {
      throw invalid("limit must be between 1 and " + MAX_LIMIT);
    }

    PipelineRunStatus parsedStatus = parseEnum(status, PipelineRunStatus.class, "status");
    NormalisedEventType parsedEventType =
        parseEnum(eventType, NormalisedEventType.class, "eventType");
    Instant fromTime = parseInstant(from, "from");
    Instant toTime = parseInstant(to, "to");
    if (fromTime != null && toTime != null && fromTime.isAfter(toTime)) {
      throw invalid("from must not be after to");
    }

    TimelineCursor decodedCursor = null;
    if (cursor != null && !cursor.isBlank()) {
      try {
        decodedCursor = TimelineCursor.decode(cursor);
      } catch (IllegalArgumentException exception) {
        throw invalid("cursor is invalid");
      }
    }

    Slice<NormalisedCiEvent> page =
        repository.searchTimeline(
            projectId,
            organisationId,
            parsedStatus,
            normaliseText(branch),
            normaliseText(commitSha),
            normaliseText(environment),
            parsedEventType,
            fromTime,
            toTime,
            decodedCursor == null ? null : decodedCursor.occurredAt(),
            decodedCursor == null ? null : decodedCursor.receivedAt(),
            decodedCursor == null ? null : decodedCursor.eventId(),
            PageRequest.of(0, limit));

    List<TimelineEvent> events =
        page.getContent().stream().map(PipelineTimelineService::toEvent).toList();
    String nextCursor = null;
    if (page.hasNext() && !events.isEmpty()) {
      TimelineEvent last = events.getLast();
      nextCursor = new TimelineCursor(last.occurredAt(), last.receivedAt(), last.id()).encode();
    }
    return new TimelinePage(events, nextCursor, page.hasNext());
  }

  private void requireProject(UUID organisationId, UUID projectId) {
    if (projectRepository.findByIdAndOrganisationId(projectId, organisationId).isEmpty()) {
      throw new TenantAccessException(
          HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "The project was not found.");
    }
  }

  private static TimelineEvent toEvent(NormalisedCiEvent event) {
    PipelineRun run = event.getPipelineRun();
    return new TimelineEvent(
        event.getId(),
        run == null ? null : run.getId(),
        event.getProvider().name(),
        event.getEventType().name(),
        event.getPipelineStatus().name(),
        event.getExternalRunId(),
        event.getPipelineName(),
        event.getRunAttempt(),
        event.getCommitSha(),
        event.getGitRef(),
        event.getEnvironmentName(),
        event.getOccurredAt(),
        event.getReceivedAt(),
        event.getEvidenceSummary());
  }

  private static String normaliseText(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private static Instant parseInstant(String value, String field) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(value);
    } catch (DateTimeParseException exception) {
      throw invalid(field + " must be an RFC 3339 instant");
    }
  }

  private static <T extends Enum<T>> T parseEnum(String value, Class<T> type, String field) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      throw invalid(field + " is not supported");
    }
  }

  private static TenantAccessException invalid(String message) {
    return new TenantAccessException(HttpStatus.BAD_REQUEST, "TIMELINE_FILTER_INVALID", message);
  }

  public record TimelinePage(List<TimelineEvent> items, String nextCursor, boolean hasNext) {}

  public record TimelineEvent(
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
