package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationCitationCoverageTest {

  @Test
  void acceptsEvidenceAndHistoricalSourcesAndExposesFields() {
    UUID id = UUID.randomUUID();
    UUID recommendationId = UUID.randomUUID();
    UUID evidenceId = UUID.randomUUID();
    UUID historicalId = UUID.randomUUID();
    RecommendationCitation evidenceCitation =
        new RecommendationCitation(id, recommendationId, evidenceId, null, "Evidence claim");
    RecommendationCitation historicalCitation =
        new RecommendationCitation(
            UUID.randomUUID(), recommendationId, null, historicalId, "Historical claim");

    assertThat(evidenceCitation.getId()).isEqualTo(id);
    assertThat(evidenceCitation.getRecommendationId()).isEqualTo(recommendationId);
    assertThat(evidenceCitation.getEvidenceId()).isEqualTo(evidenceId);
    assertThat(evidenceCitation.getHistoricalRecordId()).isNull();
    assertThat(evidenceCitation.getClaim()).isEqualTo("Evidence claim");
    assertThat(historicalCitation.getHistoricalRecordId()).isEqualTo(historicalId);
  }

  @Test
  void rejectsMissingMultipleOrUnboundedCitationSources() {
    assertThatThrownBy(
            () ->
                new RecommendationCitation(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "x"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () -> new RecommendationCitation(UUID.randomUUID(), UUID.randomUUID(), null, null, "x"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new RecommendationCitation(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, " "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new RecommendationCitation(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, "x".repeat(501)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
