package com.samharrison.incidentresponse.recommendation;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class DeterministicRecommendationProvider implements RecommendationProvider {

  public static final String PROVIDER_NAME = "deterministic-local";
  public static final String MODEL_VERSION = "rules-1";

  @Override
  public ProviderRecommendationCandidate generate(ProviderRecommendationRequest request) {
    String text =
        request.evidence().stream()
            .map(EvidenceBundleAssembler.EvidenceSummary::sanitisedContent)
            .reduce("", (left, right) -> left + " " + right)
            .toLowerCase(Locale.ROOT);
    if (text.isBlank() || text.contains("[untrusted_instruction_removed]")) {
      return abstain("INSUFFICIENT_OR_UNTRUSTED_EVIDENCE");
    }
    if (text.contains("timeout") || text.contains("connection refused")) {
      return new ProviderRecommendationCandidate(
          "dependency",
          "Investigate dependency availability and timeout behaviour.",
          "dependency availability",
          0.72,
          "A supported dependency failure signal was present.",
          RecommendationStatus.RECOMMENDED,
          null,
          PROVIDER_NAME,
          MODEL_VERSION);
    }
    if (text.contains("rollback") || text.contains("deployment failed")) {
      return new ProviderRecommendationCandidate(
          "deployment",
          "Compare the release with the last known healthy deployment.",
          "deployment regression",
          0.68,
          "A supported deployment signal was present.",
          RecommendationStatus.RECOMMENDED,
          null,
          PROVIDER_NAME,
          MODEL_VERSION);
    }
    return abstain("NO_SUPPORTED_RECOMMENDATION_SIGNAL");
  }

  private static ProviderRecommendationCandidate abstain(String reason) {
    return new ProviderRecommendationCandidate(
        "unknown",
        "No safe recommendation can be produced from the supplied evidence.",
        null,
        0.0,
        "The bounded rule set did not establish a safe recommendation.",
        RecommendationStatus.ABSTAINED,
        reason,
        PROVIDER_NAME,
        MODEL_VERSION);
  }
}
