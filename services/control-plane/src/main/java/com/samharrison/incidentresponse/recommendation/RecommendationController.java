package com.samharrison.incidentresponse.recommendation;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
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
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/recommendations")
public class RecommendationController {
  private final RecommendationService recommendationService;
  private final RecommendationGenerationService generationService;
  private final CurrentUserProvider currentUserProvider;

  public RecommendationController(
      RecommendationService recommendationService,
      RecommendationGenerationService generationService,
      CurrentUserProvider currentUserProvider) {
    this.recommendationService = recommendationService;
    this.generationService = generationService;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  RecommendationList list(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId) {
    return new RecommendationList(
        recommendationService.list(
            currentUserProvider.requireUserId(authentication), organisationId, projectId));
  }

  @PostMapping
  Recommendation generate(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @RequestBody GenerateRequest request) {
    return generationService.generate(
        currentUserProvider.requireUserId(authentication),
        organisationId,
        projectId,
        request.incidentId(),
        request.evidenceIds(),
        request.historicalRecordIds());
  }

  public record GenerateRequest(
      UUID incidentId, List<UUID> evidenceIds, List<UUID> historicalRecordIds) {
    public GenerateRequest {
      evidenceIds = evidenceIds == null ? List.of() : List.copyOf(evidenceIds);
      historicalRecordIds =
          historicalRecordIds == null ? List.of() : List.copyOf(historicalRecordIds);
    }
  }

  public record RecommendationList(List<Recommendation> items) {}
}
