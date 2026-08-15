package com.samharrison.incidentresponse.review;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReviewPersistenceTest {
  @Test
  void rejectedReviewRequiresReason() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new RecommendationReview(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    ReviewAction.REJECT,
                    ReviewReason.NONE,
                    null,
                    UUID.randomUUID(),
                    Instant.now()));
  }

  @Test
  void editedVersionRequiresBoundedSummary() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new ReviewedRecommendationVersion(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    1,
                    "x".repeat(2001),
                    null,
                    UUID.randomUUID(),
                    Instant.now()));
  }

  @Test
  void resolutionRequiresReviewedVersion() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new IncidentResolution(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    null,
                    "resolution",
                    UUID.randomUUID(),
                    Instant.now()));
  }
}
