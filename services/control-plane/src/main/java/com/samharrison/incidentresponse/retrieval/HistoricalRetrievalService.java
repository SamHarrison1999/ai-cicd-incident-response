package com.samharrison.incidentresponse.retrieval;

import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoricalRetrievalService {

  private final HistoricalRetrievalQueryService queryService;
  private final ProjectRepository projectRepository;
  private final TenantAccessService tenantAccessService;

  public HistoricalRetrievalService(
      HistoricalRetrievalQueryService queryService,
      ProjectRepository projectRepository,
      TenantAccessService tenantAccessService) {
    this.queryService = queryService;
    this.projectRepository = projectRepository;
    this.tenantAccessService = tenantAccessService;
  }

  @Transactional(readOnly = true)
  public HistoricalRetrievalPage search(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      String diagnosisCategory,
      String provider,
      String pipeline,
      String environment,
      String branch,
      String commitSha,
      String from,
      String to,
      String query,
      String cursor,
      int limit) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    if (projectRepository.findByIdAndOrganisationId(projectId, organisationId).isEmpty()) {
      throw invalid(
          HttpStatus.NOT_FOUND, "HISTORICAL_PROJECT_NOT_FOUND", "The project was not found.");
    }
    Instant occurredFrom = parseInstant(from, "from");
    Instant occurredTo = parseInstant(to, "to");
    HistoricalRetrievalCriteria criteria =
        new HistoricalRetrievalCriteria(
            normalise(diagnosisCategory),
            normalise(provider),
            normalise(pipeline),
            normalise(environment),
            normalise(branch),
            normalise(commitSha),
            occurredFrom,
            occurredTo,
            normalise(query),
            limit);
    HistoricalRetrievalCursor decoded;
    try {
      decoded = HistoricalRetrievalCursor.decode(cursor);
    } catch (IllegalArgumentException exception) {
      throw invalid(HttpStatus.BAD_REQUEST, "HISTORICAL_CURSOR_INVALID", "The cursor is invalid.");
    }
    Slice<HistoricalRetrievalRecord> page =
        queryService.search(
            organisationId,
            projectId,
            criteria,
            decoded == null ? null : decoded.occurredAt(),
            decoded == null ? null : decoded.id());
    List<HistoricalRetrievalItem> items =
        page.getContent().stream().map(HistoricalRetrievalService::toItem).toList();
    String nextCursor = null;
    if (page.hasNext() && !items.isEmpty()) {
      HistoricalRetrievalItem last = items.getLast();
      nextCursor = new HistoricalRetrievalCursor(last.occurredAt(), last.id()).encode();
    }
    return new HistoricalRetrievalPage(items, nextCursor, page.hasNext());
  }

  private static HistoricalRetrievalItem toItem(HistoricalRetrievalRecord record) {
    return new HistoricalRetrievalItem(
        record.getId(),
        record.getIncidentId(),
        record.getSourceKind().name(),
        record.getSourceId(),
        record.getOccurredAt(),
        record.getProvider(),
        record.getPipelineName(),
        record.getEnvironmentName(),
        record.getGitRef(),
        record.getCommitSha(),
        record.getDiagnosisCategory(),
        record.getSummary(),
        record.getMatchExplanation(),
        record.getProvenanceReference());
  }

  private static Instant parseInstant(String value, String field) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(value);
    } catch (DateTimeParseException exception) {
      throw invalid(
          HttpStatus.BAD_REQUEST,
          "HISTORICAL_FILTER_INVALID",
          field + " must be an RFC 3339 instant.");
    }
  }

  private static String normalise(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private static TenantAccessException invalid(HttpStatus status, String code, String message) {
    return new TenantAccessException(status, code, message);
  }

  public record HistoricalRetrievalPage(
      List<HistoricalRetrievalItem> items, String nextCursor, boolean hasNext) {}

  public record HistoricalRetrievalItem(
      UUID id,
      UUID incidentId,
      String sourceKind,
      UUID sourceId,
      Instant occurredAt,
      String provider,
      String pipelineName,
      String environmentName,
      String gitRef,
      String commitSha,
      String diagnosisCategory,
      String summary,
      String matchExplanation,
      String provenanceReference) {}
}
