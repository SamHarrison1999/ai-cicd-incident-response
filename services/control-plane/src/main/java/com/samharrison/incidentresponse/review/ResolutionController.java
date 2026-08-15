package com.samharrison.incidentresponse.review;

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
@RequestMapping(
    "/api/v1/organisations/{organisationId}/projects/{projectId}/incidents/{incidentId}/resolutions")
public class ResolutionController {
  private final ReviewApiService service;
  private final CurrentUserProvider currentUserProvider;

  public ResolutionController(ReviewApiService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  ResolutionResponse create(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID incidentId,
      @RequestBody ResolutionRequest request) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    IncidentResolution resolution =
        service.resolve(
            userId,
            organisationId,
            projectId,
            incidentId,
            request.recommendationId(),
            request.reviewedVersionId(),
            request.resolutionText());
    return new ResolutionResponse(
        resolution.getId(),
        resolution.getIncidentId(),
        resolution.getRecommendationId(),
        resolution.getReviewedVersionId(),
        resolution.getResolutionText(),
        resolution.getCreatedAt());
  }

  public record ResolutionRequest(
      UUID recommendationId, UUID reviewedVersionId, String resolutionText) {}

  public record ResolutionResponse(
      UUID id,
      UUID incidentId,
      UUID recommendationId,
      UUID reviewedVersionId,
      String resolutionText,
      Instant createdAt) {}
}
