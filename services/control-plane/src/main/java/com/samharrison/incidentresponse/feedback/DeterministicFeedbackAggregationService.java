package com.samharrison.incidentresponse.feedback;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DeterministicFeedbackAggregationService {
  private static final int MINIMUM_SAMPLE = 3;

  public FeedbackAggregate aggregate(
      UUID organisationId,
      UUID projectId,
      String policyVersion,
      Instant windowStart,
      Instant windowEnd,
      List<FeedbackSignal> signals) {
    Objects.requireNonNull(signals);
    List<FeedbackSignal> scoped =
        signals.stream()
            .filter(
                signal ->
                    organisationId.equals(signal.getOrganisationId())
                        && projectId.equals(signal.getProjectId()))
            .filter(signal -> policyVersion.equals(signal.getPolicyVersion()))
            .filter(
                signal ->
                    !signal.getCreatedAt().isBefore(windowStart)
                        && !signal.getCreatedAt().isAfter(windowEnd))
            .sorted(
                java.util.Comparator.comparing(FeedbackSignal::getCreatedAt)
                    .thenComparing(FeedbackSignal::getId))
            .toList();
    int accepted = count(scoped, FeedbackOutcome.ACCEPTED);
    int edited = count(scoped, FeedbackOutcome.EDITED);
    int rejected = count(scoped, FeedbackOutcome.REJECTED);
    int resolved = count(scoped, FeedbackOutcome.RESOLVED);
    FeedbackSuppressionReason suppression =
        scoped.size() < MINIMUM_SAMPLE
            ? FeedbackSuppressionReason.INSUFFICIENT_SAMPLE
            : FeedbackSuppressionReason.NONE;
    return new FeedbackAggregate(
        UUID.randomUUID(),
        organisationId,
        projectId,
        policyVersion,
        windowStart,
        windowEnd,
        scoped.size(),
        accepted,
        edited,
        rejected,
        resolved,
        suppression);
  }

  private static int count(List<FeedbackSignal> signals, FeedbackOutcome outcome) {
    return (int) signals.stream().filter(signal -> signal.getOutcome() == outcome).count();
  }
}
