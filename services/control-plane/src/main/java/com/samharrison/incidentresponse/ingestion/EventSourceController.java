package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/event-sources")
public class EventSourceController {
  private final EventSourceService service;
  private final CurrentUserProvider currentUserProvider;

  public EventSourceController(
      EventSourceService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  List<EventSourceResponse> list(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId) {
    return service
        .list(currentUserProvider.requireUserId(authentication), organisationId, projectId)
        .stream()
        .map(EventSourceController::toResponse)
        .toList();
  }

  @PostMapping
  ResponseEntity<EventSourceResponse> create(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @Valid @RequestBody CreateEventSourceRequest request) {
    EventSource source =
        service.create(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            request.provider(),
            request.displayName(),
            request.status(),
            request.signingSecretReference(),
            request.signatureAlgorithm(),
            request.timestampToleranceSeconds(),
            request.maxPayloadSizeBytes());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(source));
  }

  private static EventSourceResponse toResponse(EventSource source) {
    return new EventSourceResponse(
        source.getId(),
        source.getProject().getId(),
        source.getProvider().name(),
        source.getDisplayName(),
        source.getStatus().name(),
        source.getSignatureAlgorithm().name(),
        source.getTimestampToleranceSeconds(),
        source.getMaxPayloadSizeBytes(),
        source.getCreatedAt(),
        source.getUpdatedAt());
  }

  public record CreateEventSourceRequest(
      @NotNull EventProvider provider,
      @NotBlank @Size(max = 120) String displayName,
      @NotNull EventSourceStatus status,
      @NotBlank @Size(max = 255) String signingSecretReference,
      @NotNull SignatureAlgorithm signatureAlgorithm,
      @Min(1) @Max(3600) int timestampToleranceSeconds,
      @Min(1) @Max(1048576) int maxPayloadSizeBytes) {}

  public record EventSourceResponse(
      UUID id,
      UUID projectId,
      String provider,
      String displayName,
      String status,
      String signatureAlgorithm,
      int timestampToleranceSeconds,
      int maxPayloadSizeBytes,
      Instant createdAt,
      Instant updatedAt) {}
}
