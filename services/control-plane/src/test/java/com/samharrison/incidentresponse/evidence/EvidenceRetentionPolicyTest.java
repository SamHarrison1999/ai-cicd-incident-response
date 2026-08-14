package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class EvidenceRetentionPolicyTest {
  private final EvidenceRetentionPolicy policy = new EvidenceRetentionPolicy();

  @Test
  void expiresOnlyAtTheRetentionBoundary() {
    Instant ingestedAt = Instant.parse("2026-08-14T12:00:00Z");
    assertThat(
            policy.isExpired(
                RetentionClass.SHORT, ingestedAt, ingestedAt.plusSeconds(6 * 24 * 60 * 60)))
        .isFalse();
    assertThat(
            policy.isExpired(
                RetentionClass.SHORT, ingestedAt, ingestedAt.plus(RetentionClass.SHORT.duration())))
        .isTrue();
  }
}
