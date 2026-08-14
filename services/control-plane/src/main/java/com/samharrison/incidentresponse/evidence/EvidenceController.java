package com.samharrison.incidentresponse.evidence;

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
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/evidence")
public class EvidenceController {

  private final EvidenceSearchService searchService;
  private final CurrentUserProvider currentUserProvider;

  public EvidenceController(
      EvidenceSearchService searchService, CurrentUserProvider currentUserProvider) {
    this.searchService = searchService;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  EvidenceSearchResponse search(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @RequestParam(required = false) EvidenceKind kind,
      @RequestParam(required = false) String sourceSystem,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Instant occurredFrom,
      @RequestParam(required = false) Instant occurredTo,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "50") int limit) {
    EvidenceSearchCriteria criteria =
        new EvidenceSearchCriteria(kind, sourceSystem, q, occurredFrom, occurredTo, limit);
    EvidenceSearchService.SearchPage page =
        searchService.search(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            criteria,
            EvidenceCursor.decode(cursor));
    return new EvidenceSearchResponse(
        page.items().stream().map(EvidenceController::toResponse).toList(),
        page.nextCursor() == null ? null : page.nextCursor().encode());
  }

  private static EvidenceResponse toResponse(Evidence evidence) {
    return new EvidenceResponse(
        evidence.getId(),
        evidence.getKind().name(),
        evidence.getRetentionClass().name(),
        evidence.getSourceSystem(),
        evidence.getSourceReference(),
        evidence.getOccurredAt(),
        evidence.getIngestedAt(),
        evidence.getContentHash(),
        evidence.getContentLineCount());
  }

  public record EvidenceSearchResponse(List<EvidenceResponse> items, String nextCursor) {}

  public record EvidenceResponse(
      UUID id,
      String kind,
      String retentionClass,
      String sourceSystem,
      String sourceReference,
      Instant occurredAt,
      Instant ingestedAt,
      String contentHash,
      int contentLineCount) {}
}
