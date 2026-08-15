package com.samharrison.incidentresponse.review;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationReviewSecurityContractTest {
  @Test
  void rejectRequiresReason() {
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
  void resolutionTextIsBounded() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new IncidentResolution(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "x".repeat(2001),
                    UUID.randomUUID(),
                    Instant.now()));
  }
}
