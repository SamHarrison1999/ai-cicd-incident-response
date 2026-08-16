package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationServiceCoverageTest {

  @Test
  void savesARecommendationAfterTenantValidation() {
    RecommendationRepository repository = mock(RecommendationRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    Recommendation recommendation = mock(Recommendation.class);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    when(recommendation.getOrganisationId()).thenReturn(organisationId);
    when(repository.save(recommendation)).thenReturn(recommendation);

    assertThat(new RecommendationService(repository, tenant).save(userId, recommendation))
        .isSameAs(recommendation);
  }
}
