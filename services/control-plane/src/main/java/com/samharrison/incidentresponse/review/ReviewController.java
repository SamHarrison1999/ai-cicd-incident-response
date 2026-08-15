package com.samharrison.incidentresponse.review;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    "/api/v1/organisations/{organisationId}/projects/{projectId}/recommendations/{recommendationId}/reviews")
public class ReviewController {
  private final ReviewApiService service;
  private final CurrentUserProvider currentUserProvider;

  public ReviewController(ReviewApiService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  ReviewHistory history(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID recommendationId) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    return new ReviewHistory(
        service.history(userId, organisationId, projectId, recommendationId).stream()
            .map(ReviewResponse::from)
            .toList());
  }

  @PostMapping
  ReviewResponse submit(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @PathVariable UUID recommendationId,
      @RequestBody ReviewRequest request) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    return ReviewResponse.from(
        service.submit(
            userId,
            organisationId,
            projectId,
            recommendationId,
            request.action(),
            request.reason(),
            request.comment(),
            request.editedSummary(),
            request.editedCause()));
  }

  public record ReviewRequest(
      ReviewAction action,
      ReviewReason reason,
      String comment,
      String editedSummary,
      String editedCause) {}

  public record ReviewHistory(List<ReviewResponse> items) {}

  public record ReviewResponse(
      UUID id,
      String action,
      String reason,
      String comment,
      UUID reviewedVersionId,
      Instant createdAt) {
    static ReviewResponse from(RecommendationReview review) {
      return new ReviewResponse(
          review.getId(),
          review.getAction().name(),
          review.getReasonCategory().name(),
          review.getComment(),
          review.getReviewedVersionId(),
          review.getCreatedAt());
    }
  }
}
