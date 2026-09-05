package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RecommendationGenerationCitationTest {

  @Test
  void persistsEvidenceAndHistoricalCitationsForGeneratedRecommendation() {
    EvidenceBundleAssembler assembler = mock(EvidenceBundleAssembler.class);
    ProviderRecommendationRegistry registry = mock(ProviderRecommendationRegistry.class);
    RecommendationProvider provider = mock(RecommendationProvider.class);
    RecommendationService recommendationService = mock(RecommendationService.class);
    RecommendationCitationRepository citationRepository =
        mock(RecommendationCitationRepository.class);

    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID incidentId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    UUID historicalId = UUID.randomUUID();

    EvidenceBundleAssembler.EvidenceBundle bundle =
        new EvidenceBundleAssembler.EvidenceBundle(
            List.of(
                new EvidenceBundleAssembler.EvidenceSummary(
                    evidenceId,
                    "LOG_EXCERPT",
                    "portfolio-demo",
                    "a".repeat(64),
                    "connection refused")),
            List.of(
                new EvidenceBundleAssembler.HistoricalSummary(
                    historicalId, "INCIDENT", "Previous dependency outage", "historical-1")));

    when(assembler.assemble(organisationId, projectId, List.of(evidenceId), List.of(historicalId)))
        .thenReturn(bundle);

    when(registry.active()).thenReturn(provider);

    when(provider.generate(any(ProviderRecommendationRequest.class)))
        .thenReturn(
            new ProviderRecommendationCandidate(
                "dependency",
                "Investigate dependency availability and timeout behaviour.",
                "dependency availability",
                0.72,
                "A supported dependency failure signal was present.",
                RecommendationStatus.RECOMMENDED,
                null,
                "deterministic-local",
                "rules-1"));

    when(recommendationService.save(eq(userId), any(Recommendation.class)))
        .thenAnswer(invocation -> invocation.getArgument(1));

    RecommendationGenerationService service =
        new RecommendationGenerationService(
            assembler, registry, recommendationService, citationRepository);

    Recommendation recommendation =
        service.generate(
            userId,
            organisationId,
            projectId,
            incidentId,
            List.of(evidenceId),
            List.of(historicalId));

    assertThat(recommendation.getCategory()).isEqualTo("dependency");
    assertThat(recommendation.getIncidentId()).isEqualTo(incidentId);

    ArgumentCaptor<RecommendationCitation> citationCaptor =
        ArgumentCaptor.forClass(RecommendationCitation.class);

    verify(citationRepository, times(2)).save(citationCaptor.capture());

    List<RecommendationCitation> citations = citationCaptor.getAllValues();

    assertThat(citations)
        .anySatisfy(
            citation -> {
              assertThat(citation.getRecommendationId()).isEqualTo(recommendation.getId());
              assertThat(citation.getEvidenceId()).isEqualTo(evidenceId);
              assertThat(citation.getHistoricalRecordId()).isNull();
            })
        .anySatisfy(
            citation -> {
              assertThat(citation.getRecommendationId()).isEqualTo(recommendation.getId());
              assertThat(citation.getEvidenceId()).isNull();
              assertThat(citation.getHistoricalRecordId()).isEqualTo(historicalId);
            });
  }
}
