package com.samharrison.incidentresponse.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReviewedRecommendationVersionCoverageTest {
  @Test
  void acceptsOptionalCauseAndExposesVersionFields() {
    UUID organisationId = UUID.randomUUID();
    ReviewedRecommendationVersion version =
        new ReviewedRecommendationVersion(
            UUID.randomUUID(),
            organisationId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            "Reviewed summary",
            null,
            UUID.randomUUID(),
            Instant.now());
    assertThat(version.getOrganisationId()).isEqualTo(organisationId);
    assertThat(version.getLikelyCause()).isNull();
    assertThat(version.getVersionNumber()).isEqualTo(1);
  }

  @Test
  void rejectsInvalidVersionAndTextBounds() {
    assertThatThrownBy(() -> version(0, "summary", "cause"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> version(1, " ", "cause")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> version(1, "summary", " "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> version(1, "summary", "x".repeat(1001)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static ReviewedRecommendationVersion version(int number, String summary, String cause) {
    return new ReviewedRecommendationVersion(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        number,
        summary,
        cause,
        UUID.randomUUID(),
        Instant.now());
  }
}
