package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EvidenceRedactorTest {
  @Test
  void removesSecretMaterialBeforePersistence() {
    EvidenceRedactor.RedactedContent result =
        EvidenceRedactor.redact(
            "password=hidden Bearer abc.def.sig sha256=0123456789abcdef0123456789abcdef");
    assertThat(result.content()).doesNotContain("hidden", "abc.def.sig");
    assertThat(result.content()).contains("[REDACTED]");
  }

  @Test
  void rejectsBlankEvidence() {
    assertThatThrownBy(() -> EvidenceRedactor.redact(" "))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
