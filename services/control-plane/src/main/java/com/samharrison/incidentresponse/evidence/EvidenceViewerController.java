package com.samharrison.incidentresponse.evidence;

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
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/evidence")
public class EvidenceViewerController {

  private final EvidenceViewerService viewerService;
  private final CurrentUserProvider currentUserProvider;

  public EvidenceViewerController(
      EvidenceViewerService viewerService, CurrentUserProvider currentUserProvider) {
    this.viewerService = viewerService;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping("/{evidenceId}")
  EvidenceViewerResponse get(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID evidenceId) {
    EvidenceViewerService.EvidenceView view =
        viewerService.get(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            evidenceId);
    Evidence evidence = view.evidence();
    return new EvidenceViewerResponse(
        evidence.getId(),
        evidence.getKind().name(),
        evidence.getRetentionClass().name(),
        evidence.getSourceSystem(),
        evidence.getSourceReference(),
        evidence.getOccurredAt(),
        evidence.getIngestedAt(),
        evidence.getContentHash(),
        evidence.getContent(),
        evidence.getContentLineCount(),
        view.incidentIds(),
        view.eventIds());
  }

  public record EvidenceViewerResponse(
      UUID id,
      String kind,
      String retentionClass,
      String sourceSystem,
      String sourceReference,
      Instant occurredAt,
      Instant ingestedAt,
      String contentHash,
      String content,
      int contentLineCount,
      List<UUID> incidentIds,
      List<UUID> eventIds) {}
}
