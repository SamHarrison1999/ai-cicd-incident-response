package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.ingestion.NormalisedEventType;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;

public final class DeterministicCorrelationPolicy {

  public static final String VERSION = "incident-correlation-v1";
  public static final int THRESHOLD = 5;
  public static final Duration WINDOW = Duration.ofMinutes(30);

  public CorrelationScore score(CorrelationEvent event, IncidentCorrelationCandidate candidate) {
    if (!event.organisationId().equals(candidate.organisationId())
        || !event.projectId().equals(candidate.projectId())
        || !isOpen(candidate.status())) {
      return new CorrelationScore(candidate, 0, Set.of());
    }

    EnumSet<CorrelationDimension> dimensions = EnumSet.noneOf(CorrelationDimension.class);
    if (matches(event.commitSha(), candidate.commitSha())) {
      dimensions.add(CorrelationDimension.COMMIT);
    }
    if (matches(event.environmentName(), candidate.environmentName())) {
      dimensions.add(CorrelationDimension.ENVIRONMENT);
    }
    if (eventFamily(event.eventType()).equals(eventFamily(candidate.eventType()))) {
      dimensions.add(CorrelationDimension.EVENT_FAMILY);
    }
    if (event.externalRunId().equals(candidate.externalRunId())
        && event.attempt() == candidate.attempt()) {
      dimensions.add(CorrelationDimension.PIPELINE_RUN);
    }
    if (Duration.between(candidate.detectedAt(), event.occurredAt()).abs().compareTo(WINDOW) <= 0) {
      dimensions.add(CorrelationDimension.TIME_WINDOW);
    }

    return new CorrelationScore(candidate, weight(dimensions), Set.copyOf(dimensions));
  }

  public boolean isEligible(CorrelationEvent event) {
    return event.pipelineStatus()
            == com.samharrison.incidentresponse.ingestion.PipelineRunStatus.FAILED
        || event.pipelineStatus()
            == com.samharrison.incidentresponse.ingestion.PipelineRunStatus.CANCELLED
        || event.pipelineStatus()
            == com.samharrison.incidentresponse.ingestion.PipelineRunStatus.TIMED_OUT;
  }

  private static boolean isOpen(IncidentStatus status) {
    return status != IncidentStatus.RESOLVED;
  }

  private static boolean matches(String left, String right) {
    return left != null && !left.isBlank() && right != null && left.equals(right);
  }

  private static int weight(Set<CorrelationDimension> dimensions) {
    return dimensions.stream()
        .mapToInt(
            dimension ->
                switch (dimension) {
                  case COMMIT -> 3;
                  case ENVIRONMENT -> 2;
                  case EVENT_FAMILY -> 2;
                  case PIPELINE_RUN -> 1;
                  case TIME_WINDOW -> 1;
                })
        .sum();
  }

  private static String eventFamily(NormalisedEventType eventType) {
    return eventType.name().startsWith("DEPLOYMENT_") ? "DEPLOYMENT" : "PIPELINE";
  }

  public record CorrelationScore(
      IncidentCorrelationCandidate candidate, int score, Set<CorrelationDimension> dimensions) {}
}
