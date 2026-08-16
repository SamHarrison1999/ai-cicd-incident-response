package com.samharrison.incidentresponse.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class JenkinsEventAdapter implements ProviderEventAdapter {

  @Override
  public EventProvider provider() {
    return EventProvider.JENKINS;
  }

  @Override
  public Optional<NormalisedEventCandidate> adapt(
      String providerEventType, JsonNode payload, Instant fallbackOccurredAt) {
    JsonNode build = payload.has("build") ? payload.path("build") : payload;
    String externalRunId = firstText(build, "number", "id");
    if (externalRunId == null && !payload.has("name")) {
      return Optional.empty();
    }
    if (externalRunId == null) {
      externalRunId = "jenkins-" + Integer.toHexString(payload.toString().hashCode());
    }

    String result = firstText(build, "result", "status");
    boolean completed = result != null;
    PipelineRunStatus pipelineStatus =
        completed ? completedStatus(result) : PipelineRunStatus.RUNNING;
    NormalisedEventType eventType =
        completed
            ? NormalisedEventType.PIPELINE_RUN_COMPLETED
            : NormalisedEventType.PIPELINE_RUN_STARTED;
    String name = firstText(build, "fullDisplayName", "name");
    if (name == null) {
      name = firstText(payload, "name");
    }
    if (name == null) {
      name = "Jenkins build " + externalRunId;
    }

    List<String> sourceFields = new ArrayList<>();
    addPresent(sourceFields, build, "number");
    addPresent(sourceFields, build, "id");
    addPresent(sourceFields, build, "result");
    addPresent(sourceFields, build, "timestamp");
    addPresent(sourceFields, build, "fullDisplayName");
    addPresent(sourceFields, payload, "name");

    return Optional.of(
        new NormalisedEventCandidate(
            eventType,
            occurredAt(build, fallbackOccurredAt),
            externalRunId,
            name,
            1,
            pipelineStatus,
            nestedText(payload, "scm", "commitId"),
            nestedText(payload, "scm", "branch"),
            null,
            "Jenkins build fields were mapped deterministically.",
            sourceFields));
  }

  private PipelineRunStatus completedStatus(String value) {
    return switch (value.toUpperCase(Locale.ROOT)) {
      case "SUCCESS" -> PipelineRunStatus.SUCCEEDED;
      case "FAILURE", "UNSTABLE" -> PipelineRunStatus.FAILED;
      case "ABORTED" -> PipelineRunStatus.CANCELLED;
      case "NOT_BUILT" -> PipelineRunStatus.SKIPPED;
      default -> PipelineRunStatus.UNKNOWN;
    };
  }

  private Instant occurredAt(JsonNode build, Instant fallback) {
    JsonNode timestamp = build.path("timestamp");
    if (timestamp.isNumber()) {
      return Instant.ofEpochMilli(timestamp.asLong());
    }
    String value = firstText(build, "startedAt", "completedAt", "timestamp");
    if (value != null) {
      try {
        return Instant.parse(value);
      } catch (DateTimeParseException ignored) {
        // Use receipt time for provider timestamps that are not RFC 3339.
      }
    }
    return fallback;
  }

  private static String nestedText(JsonNode node, String parent, String child) {
    JsonNode value = node.path(parent).path(child);
    return value.isTextual() || value.isNumber() ? value.asText() : null;
  }

  private static String firstText(JsonNode node, String... fields) {
    for (String field : fields) {
      JsonNode value = node.path(field);
      if (value.isTextual() || value.isNumber()) {
        String text = value.asText();
        if (!text.isBlank()) {
          return text;
        }
      }
    }
    return null;
  }

  private static void addPresent(List<String> fields, JsonNode node, String field) {
    if (node.hasNonNull(field)) {
      fields.add(field);
    }
  }
}
