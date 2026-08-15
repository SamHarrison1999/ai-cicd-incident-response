package com.samharrison.incidentresponse.feedback;

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
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/feedback")
public class FeedbackController {
  private final FeedbackApiService service;
  private final CurrentUserProvider currentUserProvider;

  public FeedbackController(FeedbackApiService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  FeedbackResponse list(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @RequestParam(required = false) String policyVersion,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(defaultValue = "50") int limit) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    FeedbackQueryCriteria criteria = new FeedbackQueryCriteria(policyVersion, from, to, limit);
    return new FeedbackResponse(
        service.list(userId, organisationId, projectId, criteria).stream()
            .map(FeedbackItem::from)
            .toList());
  }

  public record FeedbackResponse(List<FeedbackItem> items) {}

  public record FeedbackItem(
      UUID id,
      String policyVersion,
      Instant windowStart,
      Instant windowEnd,
      int sampleCount,
      int acceptedCount,
      int editedCount,
      int rejectedCount,
      int resolvedCount,
      String suppressionReason) {
    static FeedbackItem from(FeedbackAggregate item) {
      return new FeedbackItem(
          item.getId(),
          item.getPolicyVersion(),
          item.getWindowStart(),
          item.getWindowEnd(),
          item.getSampleCount(),
          item.getAcceptedCount(),
          item.getEditedCount(),
          item.getRejectedCount(),
          item.getResolvedCount(),
          item.getSuppressionReason().name());
    }
  }
}
