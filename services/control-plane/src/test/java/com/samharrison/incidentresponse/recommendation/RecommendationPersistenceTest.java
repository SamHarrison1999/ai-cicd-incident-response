package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationPersistenceTest {

  @Test
  void rejectsUnboundedSummary() {
    String summary = "x".repeat(2001);
    assertThatIllegalArgumentException()
        .isThrownBy(() -> recommendation(summary, RecommendationStatus.RECOMMENDED, null));
  }

  @Test
  void requiresReasonWhenAbstained() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> recommendation("bounded", RecommendationStatus.ABSTAINED, null));
  }

  @Test
  void citationRequiresExactlyOneSource() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                new RecommendationCitation(
                    UUID.randomUUID(), UUID.randomUUID(), null, null, "claim"));
  }

  private static Recommendation recommendation(
      String summary, RecommendationStatus status, String reason) {
    return new Recommendation(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        null,
        "category",
        summary,
        null,
        BigDecimal.valueOf(0.5),
        "bounded confidence",
        status,
        reason,
        "deterministic",
        "1",
        "1",
        "1",
        "1",
        "1",
        Instant.now());
  }
}
