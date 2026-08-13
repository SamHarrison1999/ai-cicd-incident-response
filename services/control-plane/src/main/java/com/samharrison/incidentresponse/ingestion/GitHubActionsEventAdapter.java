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
class GitHubActionsEventAdapter implements ProviderEventAdapter {

  @Override
  public EventProvider provider() {
    return EventProvider.GITHUB_ACTIONS;
  }

  @Override
  public Optional<NormalisedEventCandidate> adapt(
      String providerEventType, JsonNode payload, Instant fallbackOccurredAt) {
    if (!providerEventType.toLowerCase(Locale.ROOT).contains("workflow_run")) {
      return Optional.empty();
    }

    JsonNode run = payload.has("workflow_run") ? payload.path("workflow_run") : payload;
    String externalRunId = text(run, "id");
    if (externalRunId == null) {
      return Optional.empty();
    }

    String conclusion = text(run, "conclusion");
    String status = text(run, "status");
    boolean completed = conclusion != null || "completed".equalsIgnoreCase(status);
    PipelineRunStatus pipelineStatus =
        completed ? completedStatus(conclusion) : runningStatus(status);
    NormalisedEventType eventType =
        completed
            ? NormalisedEventType.PIPELINE_RUN_COMPLETED
            : NormalisedEventType.PIPELINE_RUN_STARTED;
    Instant occurredAt =
        firstInstant(
            run, fallbackOccurredAt, "updated_at", "completed_at", "run_started_at", "created_at");
    String name = firstText(run, "name", "display_title");
    if (name == null) {
      name = "GitHub Actions workflow " + externalRunId;
    }
    int attempt = positiveInt(run, "run_attempt", 1);
    String commitSha = firstText(run, "head_sha", "head_commit");
    String gitRef = firstText(run, "head_branch", "ref");
    List<String> sourceFields = new ArrayList<>();
    addPresent(sourceFields, run, "id");
    addPresent(sourceFields, run, "name");
    addPresent(sourceFields, run, "run_attempt");
    addPresent(sourceFields, run, "status");
    addPresent(sourceFields, run, "conclusion");
    addPresent(sourceFields, run, "head_sha");
    addPresent(sourceFields, run, "head_branch");

    return Optional.of(
        new NormalisedEventCandidate(
            eventType,
            occurredAt,
            externalRunId,
            name,
            attempt,
            pipelineStatus,
            commitSha,
            gitRef,
            null,
            "GitHub Actions workflow_run fields were mapped deterministically.",
            sourceFields));
  }

  private PipelineRunStatus runningStatus(String value) {
    return switch (value == null ? "" : value.toLowerCase(Locale.ROOT)) {
      case "queued", "requested", "waiting" -> PipelineRunStatus.QUEUED;
      case "in_progress", "running" -> PipelineRunStatus.RUNNING;
      default -> PipelineRunStatus.UNKNOWN;
    };
  }

  private PipelineRunStatus completedStatus(String value) {
    return switch (value == null ? "" : value.toLowerCase(Locale.ROOT)) {
      case "success", "successful" -> PipelineRunStatus.SUCCEEDED;
      case "failure", "failed", "startup_failure" -> PipelineRunStatus.FAILED;
      case "cancelled", "canceled" -> PipelineRunStatus.CANCELLED;
      case "skipped" -> PipelineRunStatus.SKIPPED;
      case "timed_out", "timeout" -> PipelineRunStatus.TIMED_OUT;
      default -> PipelineRunStatus.UNKNOWN;
    };
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return value.isTextual() || value.isNumber() ? value.asText() : null;
  }

  private static String firstText(JsonNode node, String... fields) {
    for (String field : fields) {
      String value = text(node, field);
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  private static Instant firstInstant(JsonNode node, Instant fallback, String... fields) {
    for (String field : fields) {
      String value = text(node, field);
      if (value != null) {
        try {
          return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
          // Continue to the next provider field and then use the receipt time.
        }
      }
    }
    return fallback;
  }

  private static int positiveInt(JsonNode node, String field, int fallback) {
    int value = node.path(field).asInt(fallback);
    return value > 0 ? value : fallback;
  }

  private static void addPresent(List<String> fields, JsonNode node, String field) {
    if (node.hasNonNull(field)) {
      fields.add("workflow_run." + field);
    }
  }
}
