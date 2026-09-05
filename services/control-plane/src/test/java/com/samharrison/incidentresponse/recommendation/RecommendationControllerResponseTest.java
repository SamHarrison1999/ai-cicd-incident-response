package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class RecommendationControllerResponseTest {

  @Test
  void listPreservesRecommendationFieldsAndIncludesCitationCount() {
    RecommendationService recommendationService = mock(RecommendationService.class);
    RecommendationGenerationService generationService = mock(RecommendationGenerationService.class);
    RecommendationCitationRepository citationRepository =
        mock(RecommendationCitationRepository.class);
    CurrentUserProvider users = mock(CurrentUserProvider.class);
    Authentication authentication = mock(Authentication.class);

    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID incidentId = UUID.randomUUID();
    UUID recommendationId = UUID.randomUUID();
    Instant generatedAt = Instant.parse("2026-09-05T13:32:57Z");

    Recommendation recommendation =
        new Recommendation(
            recommendationId,
            organisationId,
            projectId,
            incidentId,
            "dependency",
            "Investigate dependency availability and timeout behaviour.",
            "dependency availability",
            new BigDecimal("0.720"),
            "A supported dependency failure signal was present.",
            RecommendationStatus.RECOMMENDED,
            null,
            "deterministic-local",
            "rules-1",
            "phase-9-prompt-1",
            "phase-9-rules-1",
            "retrieval-1",
            "recommendation-1",
            generatedAt);

    when(users.requireUserId(authentication)).thenReturn(userId);

    when(recommendationService.list(userId, organisationId, projectId))
        .thenReturn(List.of(recommendation));

    when(citationRepository.countByRecommendationId(recommendationId)).thenReturn(1L);

    RecommendationController controller =
        new RecommendationController(
            recommendationService, generationService, citationRepository, users);

    RecommendationController.RecommendationList result =
        controller.list(authentication, organisationId, projectId);

    assertThat(result.items()).hasSize(1);

    RecommendationController.RecommendationResponse response = result.items().getFirst();

    assertThat(response.id()).isEqualTo(recommendationId);
    assertThat(response.organisationId()).isEqualTo(organisationId);
    assertThat(response.projectId()).isEqualTo(projectId);
    assertThat(response.incidentId()).isEqualTo(incidentId);
    assertThat(response.category()).isEqualTo("dependency");
    assertThat(response.confidence()).isEqualByComparingTo("0.720");
    assertThat(response.generatedAt()).isEqualTo(generatedAt);
    assertThat(response.createdAt()).isEqualTo(generatedAt);
    assertThat(response.citations()).isEqualTo(1);
  }
}
