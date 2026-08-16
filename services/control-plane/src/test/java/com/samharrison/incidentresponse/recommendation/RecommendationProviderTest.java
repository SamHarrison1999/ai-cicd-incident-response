package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecommendationProviderTest {
  @Test
  void deterministicProviderAbstainsForUntrustedInput() {
    var evidence =
        new EvidenceBundleAssembler.EvidenceSummary(
            UUID.randomUUID(), "LOG", "ci", "hash", "[untrusted_instruction_removed]");
    var request =
        new ProviderRecommendationRequest(
            UUID.randomUUID(), UUID.randomUUID(), null, List.of(evidence), List.of(), "rules");
    assertThat(new DeterministicRecommendationProvider().generate(request).status())
        .isEqualTo(RecommendationStatus.ABSTAINED);
  }

  @Test
  void deterministicProviderAbstainsForEmptyEvidenceAndCoversAlternativeSignals() {
    ProviderRecommendationRequest empty =
        new ProviderRecommendationRequest(
            UUID.randomUUID(), UUID.randomUUID(), null, List.of(), List.of(), "rules");
    assertThat(new DeterministicRecommendationProvider().generate(empty).status())
        .isEqualTo(RecommendationStatus.ABSTAINED);
    for (String text : List.of("connection refused", "rollback")) {
      var evidence =
          new EvidenceBundleAssembler.EvidenceSummary(UUID.randomUUID(), "LOG", "ci", "hash", text);
      var request =
          new ProviderRecommendationRequest(
              UUID.randomUUID(), UUID.randomUUID(), null, List.of(evidence), List.of(), "rules");
      assertThat(new DeterministicRecommendationProvider().generate(request).status())
          .isEqualTo(RecommendationStatus.RECOMMENDED);
    }
  }

  @Test
  void deterministicProviderReturnsStableDependencyRecommendation() {
    var evidence =
        new EvidenceBundleAssembler.EvidenceSummary(
            UUID.randomUUID(), "LOG", "ci", "hash", "upstream timeout");
    var request =
        new ProviderRecommendationRequest(
            UUID.randomUUID(), UUID.randomUUID(), null, List.of(evidence), List.of(), "rules");
    assertThat(new DeterministicRecommendationProvider().generate(request).category())
        .isEqualTo("dependency");
  }

  @Test
  void deterministicProviderReturnsDeploymentRecommendation() {
    var evidence =
        new EvidenceBundleAssembler.EvidenceSummary(
            UUID.randomUUID(), "LOG", "ci", "hash", "deployment failed");
    var request =
        new ProviderRecommendationRequest(
            UUID.randomUUID(), UUID.randomUUID(), null, List.of(evidence), List.of(), "rules");
    assertThat(new DeterministicRecommendationProvider().generate(request).category())
        .isEqualTo("deployment");
  }
}
