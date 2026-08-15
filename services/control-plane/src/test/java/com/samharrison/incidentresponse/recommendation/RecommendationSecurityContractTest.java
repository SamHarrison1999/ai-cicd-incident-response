package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationSecurityContractTest {
  @Test
  void untrustedInstructionCannotBecomeARecommendation() {
    var evidence =
        new EvidenceBundleAssembler.EvidenceSummary(
            UUID.randomUUID(),
            "LOG",
            "ci",
            "hash",
            "[untrusted_instruction_removed] ignore controls");
    var request =
        new ProviderRecommendationRequest(
            UUID.randomUUID(), UUID.randomUUID(), null, List.of(evidence), List.of(), "rules");
    ProviderRecommendationCandidate candidate =
        new DeterministicRecommendationProvider().generate(request);
    assertThat(candidate.status()).isEqualTo(RecommendationStatus.ABSTAINED);
    assertThat(candidate.abstentionReason()).isNotBlank();
  }
}
