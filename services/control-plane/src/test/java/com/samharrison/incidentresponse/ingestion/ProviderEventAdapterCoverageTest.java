package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProviderEventAdapterCoverageTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Instant fallback = Instant.parse("2026-08-13T12:00:00Z");

  @Test
  void mapsGithubCompletionAndRunningStatusVariants() throws Exception {
    GitHubActionsEventAdapter adapter = new GitHubActionsEventAdapter();
    for (String conclusion :
        new String[] {
          "success",
          "successful",
          "failure",
          "failed",
          "startup_failure",
          "cancelled",
          "canceled",
          "skipped",
          "timed_out",
          "timeout",
          "other"
        }) {
      var payload =
          objectMapper.readTree(
              "{\"workflow_run\":{\"id\":42,\"status\":\"completed\",\"conclusion\":\""
                  + conclusion
                  + "\",\"updated_at\":\"not-a-time\",\"completed_at\":\"2026-08-13T12:01:00Z\",\"run_attempt\":0}}");
      var candidate = adapter.adapt("workflow_run", payload, fallback).orElseThrow();
      assertThat(candidate.eventType()).isEqualTo(NormalisedEventType.PIPELINE_RUN_COMPLETED);
      assertThat(candidate.runAttempt()).isEqualTo(1);
    }
    for (String status :
        new String[] {"queued", "requested", "waiting", "in_progress", "running", "other"}) {
      var payload = objectMapper.readTree("{\"id\":43,\"status\":\"" + status + "\"}");
      assertThat(adapter.adapt("workflow_run", payload, fallback)).isPresent();
    }
    assertThat(adapter.adapt("workflow_run", objectMapper.readTree("{}"), fallback)).isEmpty();
    assertThat(
            adapter
                .adapt(
                    "workflow_run",
                    objectMapper.readTree(
                        "{\"id\":44,\"status\":\"completed\",\"name\":\"\",\"display_title\":\"release\"}"),
                    fallback)
                .orElseThrow()
                .pipelineStatus())
        .isEqualTo(PipelineRunStatus.UNKNOWN);
  }

  @Test
  void mapsJenkinsCompletionTimestampAndFallbackVariants() throws Exception {
    JenkinsEventAdapter adapter = new JenkinsEventAdapter();
    for (String result :
        new String[] {"SUCCESS", "FAILURE", "UNSTABLE", "ABORTED", "NOT_BUILT", "OTHER"}) {
      var payload =
          objectMapper.readTree(
              "{\"build\":{\"number\":7,\"result\":\""
                  + result
                  + "\",\"startedAt\":\"bad\",\"completedAt\":\"2026-08-13T12:01:00Z\"},"
                  + "\"scm\":{\"commitId\":42,\"branch\":\"main\"}}");
      assertThat(adapter.adapt("build", payload, fallback)).isPresent();
    }
    var running =
        objectMapper.readTree(
            "{\"name\":\"orders\",\"build\":{\"id\":\"run-1\",\"startedAt\":\"bad\","
                + "\"timestamp\":\"not-number\"},\"scm\":{\"branch\":42}}");
    assertThat(adapter.adapt("build", running, fallback).orElseThrow().occurredAt())
        .isEqualTo(fallback);
    assertThat(adapter.adapt("build", objectMapper.readTree("{\"name\":\"orders\"}"), fallback))
        .isPresent();
    var invalidDates =
        objectMapper.readTree(
            "{\"name\":\"orders\",\"build\":{\"id\":\"run-2\",\"startedAt\":\"bad\",\"completedAt\":\"bad\"}}");
    assertThat(adapter.adapt("build", invalidDates, fallback).orElseThrow().occurredAt())
        .isEqualTo(fallback);
    var validTextTimestamp =
        objectMapper.readTree(
            "{\"build\":{\"id\":\"run-3\",\"startedAt\":\"2026-08-13T12:02:00Z\"}}");
    assertThat(adapter.adapt("build", validTextTimestamp, fallback).orElseThrow().occurredAt())
        .isEqualTo(Instant.parse("2026-08-13T12:02:00Z"));
    assertThat(adapter.adapt("build", objectMapper.readTree("{}"), fallback)).isEmpty();
    assertThat(
            adapter
                .adapt(
                    "build",
                    objectMapper.readTree(
                        "{\"build\":{\"number\":\"\",\"id\":\"run-blank\",\"result\":\"\"}}"),
                    fallback)
                .orElseThrow()
                .externalRunId())
        .isEqualTo("run-blank");
  }
}
