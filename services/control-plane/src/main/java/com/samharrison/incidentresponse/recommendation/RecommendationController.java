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
  private final RecommendationCitationRepository citationRepository;
  private final CurrentUserProvider currentUserProvider;

  public RecommendationController(
      RecommendationService recommendationService,
      RecommendationGenerationService generationService,
      RecommendationCitationRepository citationRepository,
      CurrentUserProvider currentUserProvider) {
    this.recommendationService = recommendationService;
    this.generationService = generationService;
    this.citationRepository = citationRepository;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  RecommendationList list(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId) {
    return new RecommendationList(
        recommendationService
            .list(currentUserProvider.requireUserId(authentication), organisationId, projectId)
            .stream()
            .map(this::toResponse)
            .toList());
  }

  @PostMapping
  RecommendationResponse generate(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @RequestBody GenerateRequest request) {
    Recommendation recommendation =
        generationService.generate(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            request.incidentId(),
            request.evidenceIds(),
            request.historicalRecordIds());

    return toResponse(recommendation);
  }

  private RecommendationResponse toResponse(Recommendation recommendation) {
    return new RecommendationResponse(
        recommendation.getId(),
        recommendation.getOrganisationId(),
        recommendation.getProjectId(),
        recommendation.getIncidentId(),
        recommendation.getCategory(),
        recommendation.getSummary(),
        recommendation.getLikelyCause(),
        recommendation.getConfidence(),
        recommendation.getConfidenceExplanation(),
        recommendation.getStatus(),
        recommendation.getAbstentionReason(),
        recommendation.getProviderName(),
        recommendation.getModelVersion(),
        recommendation.getPromptTemplateVersion(),
        recommendation.getRulesetVersion(),
        recommendation.getRetrievalSetVersion(),
        recommendation.getSchemaVersion(),
        recommendation.getGeneratedAt(),
        recommendation.getCreatedAt(),
        citationRepository.countByRecommendationId(recommendation.getId()));
  }

  public record RecommendationResponse(
      UUID id,
      UUID organisationId,
      UUID projectId,
      UUID incidentId,
      String category,
      String summary,
      String likelyCause,
      java.math.BigDecimal confidence,
      String confidenceExplanation,
      RecommendationStatus status,
      String abstentionReason,
      String providerName,
      String modelVersion,
      String promptTemplateVersion,
      String rulesetVersion,
      String retrievalSetVersion,
      String schemaVersion,
      java.time.Instant generatedAt,
      java.time.Instant createdAt,
      long citations) {}

  public record GenerateRequest(
      UUID incidentId, List<UUID> evidenceIds, List<UUID> historicalRecordIds) {
    public GenerateRequest {
      evidenceIds = evidenceIds == null ? List.of() : List.copyOf(evidenceIds);
      historicalRecordIds =
          historicalRecordIds == null ? List.of() : List.copyOf(historicalRecordIds);
    }
  }

  public record RecommendationList(List<RecommendationResponse> items) {}
}
