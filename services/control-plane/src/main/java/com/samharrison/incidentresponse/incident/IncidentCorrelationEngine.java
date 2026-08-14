package com.samharrison.incidentresponse.incident;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class IncidentCorrelationEngine {

  private final DeterministicCorrelationPolicy policy;

  public IncidentCorrelationEngine() {
    this(new DeterministicCorrelationPolicy());
  }

  IncidentCorrelationEngine(DeterministicCorrelationPolicy policy) {
    this.policy = policy;
  }

  public CorrelationDecision evaluate(
      CorrelationEvent event, List<IncidentCorrelationCandidate> candidates) {
    List<UUID> consideredCandidates =
        candidates.stream()
            .filter(
                candidate ->
                    event.organisationId().equals(candidate.organisationId())
                        && event.projectId().equals(candidate.projectId()))
            .map(IncidentCorrelationCandidate::incidentId)
            .sorted()
            .collect(Collectors.toUnmodifiableList());

    if (!policy.isEligible(event)) {
      return new CorrelationDecision(
          event.eventId(),
          null,
          DeterministicCorrelationPolicy.VERSION,
          0,
          DeterministicCorrelationPolicy.THRESHOLD,
          java.util.Set.of(),
          consideredCandidates,
          false);
    }

    return candidates.stream()
        .map(candidate -> policy.score(event, candidate))
        .filter(score -> score.score() >= DeterministicCorrelationPolicy.THRESHOLD)
        .sorted(
            Comparator.comparingInt(DeterministicCorrelationPolicy.CorrelationScore::score)
                .reversed()
                .thenComparing(score -> score.candidate().detectedAt())
                .thenComparing(score -> score.candidate().incidentId()))
        .findFirst()
        .map(
            score ->
                new CorrelationDecision(
                    event.eventId(),
                    score.candidate().incidentId(),
                    DeterministicCorrelationPolicy.VERSION,
                    score.score(),
                    DeterministicCorrelationPolicy.THRESHOLD,
                    score.dimensions(),
                    consideredCandidates,
                    true))
        .orElseGet(
            () ->
                new CorrelationDecision(
                    event.eventId(),
                    null,
                    DeterministicCorrelationPolicy.VERSION,
                    0,
                    DeterministicCorrelationPolicy.THRESHOLD,
                    java.util.Set.of(),
                    consideredCandidates,
                    true));
  }
}
