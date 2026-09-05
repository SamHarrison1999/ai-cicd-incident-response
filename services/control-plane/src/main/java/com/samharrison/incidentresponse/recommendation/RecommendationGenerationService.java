package com.samharrison.incidentresponse.recommendation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationGenerationService {
  private final EvidenceBundleAssembler assembler;
  private final ProviderRecommendationRegistry registry;
  private final RecommendationService recommendationService;
  private final RecommendationCitationRepository citationRepository;

  public RecommendationGenerationService(
      EvidenceBundleAssembler assembler,
      ProviderRecommendationRegistry registry,
      RecommendationService recommendationService,
      RecommendationCitationRepository citationRepository) {
    this.assembler = assembler;
    this.registry = registry;
    this.recommendationService = recommendationService;
    this.citationRepository = citationRepository;
  }

  @Transactional
  public Recommendation generate(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      UUID incidentId,
      List<UUID> evidenceIds,
      List<UUID> historicalIds) {
    EvidenceBundleAssembler.EvidenceBundle bundle =
        assembler.assemble(organisationId, projectId, evidenceIds, historicalIds);
    ProviderRecommendationCandidate candidate =
        registry
            .active()
            .generate(
                new ProviderRecommendationRequest(
                    organisationId,
                    projectId,
                    incidentId,
                    bundle.evidence(),
                    bundle.historical(),
                    "phase-9-rules-1"));
    Recommendation recommendation =
        new Recommendation(
            UUID.randomUUID(),
            organisationId,
            projectId,
            incidentId,
            candidate.category(),
            candidate.summary(),
            candidate.likelyCause(),
            BigDecimal.valueOf(candidate.confidence()),
            candidate.confidenceExplanation(),
            candidate.status(),
            candidate.abstentionReason(),
            candidate.providerName(),
            candidate.modelVersion(),
            "phase-9-prompt-1",
            "phase-9-rules-1",
            "retrieval-1",
            "recommendation-1",
            Instant.now());
    Recommendation saved = recommendationService.save(userId, recommendation);

    for (EvidenceBundleAssembler.EvidenceSummary evidence : bundle.evidence()) {
      citationRepository.save(
          new RecommendationCitation(
              UUID.randomUUID(),
              saved.getId(),
              evidence.id(),
              null,
              "Evidence supplied to bounded recommendation generation."));
    }

    for (EvidenceBundleAssembler.HistoricalSummary historical : bundle.historical()) {
      citationRepository.save(
          new RecommendationCitation(
              UUID.randomUUID(),
              saved.getId(),
              null,
              historical.id(),
              "Historical record supplied to bounded recommendation generation."));
    }

    return saved;
  }
}
