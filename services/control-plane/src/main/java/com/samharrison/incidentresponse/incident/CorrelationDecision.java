package com.samharrison.incidentresponse.incident;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CorrelationDecision(
    UUID eventId,
    UUID selectedIncidentId,
    String policyVersion,
    int score,
    int threshold,
    Set<CorrelationDimension> matchedDimensions,
    List<UUID> consideredCandidates,
    boolean eligible) {

  public CorrelationDecision {
    matchedDimensions = Set.copyOf(matchedDimensions);
    consideredCandidates = List.copyOf(consideredCandidates);
  }
}
