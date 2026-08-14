package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/evidence")
public class EvidenceLinkController {

  private final EvidenceLinkService service;
  private final CurrentUserProvider currentUserProvider;

  public EvidenceLinkController(
      EvidenceLinkService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping("/{evidenceId}/incident-link")
  EvidenceLinkResponse linkIncident(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID evidenceId,
      @RequestBody LinkRequest request) {
    IncidentEvidenceLink link =
        service.linkToIncident(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            evidenceId,
            request.targetId());
    return new EvidenceLinkResponse(
        link.getId(), link.getEvidence().getId(), link.getIncident().getId(), link.getLinkedAt());
  }

  @PostMapping("/{evidenceId}/event-link")
  EvidenceLinkResponse linkEvent(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID evidenceId,
      @RequestBody LinkRequest request) {
    EvidenceEventLink link =
        service.linkToEvent(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            evidenceId,
            request.targetId());
    return new EvidenceLinkResponse(
        link.getId(), link.getEvidence().getId(), link.getEvent().getId(), link.getLinkedAt());
  }

  public record LinkRequest(UUID targetId) {}

  public record EvidenceLinkResponse(
      UUID linkId, UUID evidenceId, UUID targetId, Instant linkedAt) {}
}
