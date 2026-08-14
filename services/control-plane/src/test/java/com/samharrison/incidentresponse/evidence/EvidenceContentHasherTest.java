package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EvidenceContentHasherTest {
  @Test
  void producesStableLowercaseSha256Hex() {
    assertThat(EvidenceContentHasher.sha256Hex("same evidence"))
        .hasSize(64)
        .matches("[0-9a-f]{64}");
    assertThat(EvidenceContentHasher.sha256Hex("same evidence"))
        .isEqualTo(EvidenceContentHasher.sha256Hex("same evidence"));
  }
}
