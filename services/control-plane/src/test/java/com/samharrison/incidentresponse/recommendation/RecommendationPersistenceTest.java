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
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                recommendation(
                    "bounded", RecommendationStatus.RECOMMENDED, null, BigDecimal.valueOf(-0.1)));
  }

  @Test
  void requiresReasonWhenAbstained() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> recommendation("bounded", RecommendationStatus.ABSTAINED, null));
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                recommendation(
                    "bounded", RecommendationStatus.RECOMMENDED, null, BigDecimal.valueOf(-0.1)));

    Recommendation abstained =
        recommendation("bounded", RecommendationStatus.ABSTAINED, "insufficient evidence");
    org.assertj.core.api.Assertions.assertThat(abstained.getAbstentionReason())
        .isEqualTo("insufficient evidence");
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
    return recommendation(summary, status, reason, BigDecimal.valueOf(0.5));
  }

  private static Recommendation recommendation(
      String summary, RecommendationStatus status, String reason, BigDecimal confidence) {
    return new Recommendation(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        null,
        "category",
        summary,
        null,
        confidence,
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
