package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/incidents")
public class IncidentController {

  private final IncidentService service;
  private final CurrentUserProvider currentUserProvider;

  public IncidentController(IncidentService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  List<IncidentResponse> list(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    return service.list(userId, organisationId, projectId).stream()
        .map(IncidentController::toResponse)
        .toList();
  }

  @GetMapping("/{incidentId}")
  IncidentResponse get(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID incidentId) {
    return toResponse(
        service.get(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            incidentId));
  }

  @PatchMapping("/{incidentId}/status")
  IncidentResponse transition(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID incidentId,
      @RequestBody IncidentStatusRequest request) {
    Instant occurredAt = request.occurredAt() == null ? Instant.now() : request.occurredAt();
    return toResponse(
        service.transition(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            incidentId,
            request.status(),
            occurredAt));
  }

  private static IncidentResponse toResponse(Incident incident) {
    return new IncidentResponse(
        incident.getId(),
        incident.getStatus().name(),
        incident.getTitle(),
        incident.getSummary(),
        incident.getDetectedAt(),
        incident.getResolvedAt(),
        incident.getCreatedAt(),
        incident.getUpdatedAt());
  }

  public record IncidentStatusRequest(IncidentStatus status, Instant occurredAt) {}

  public record IncidentResponse(
      UUID id,
      String status,
      String title,
      String summary,
      Instant detectedAt,
      Instant resolvedAt,
      Instant createdAt,
      Instant updatedAt) {}
}
