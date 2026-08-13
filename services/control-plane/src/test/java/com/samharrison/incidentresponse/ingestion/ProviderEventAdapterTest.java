package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ProviderEventAdapterTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Instant fallback = Instant.parse("2026-08-13T12:00:00Z");

  @Test
  void mapsGithubActionsFailureToTerminalNormalisedEvent() throws Exception {
    GitHubActionsEventAdapter adapter = new GitHubActionsEventAdapter();
    var payload =
        objectMapper.readTree(
"""
            {
  "action":"completed",
  "workflow_run":{
    "id":42,
    "name":"build",
    "run_attempt":2,
    "status":"completed",
    "conclusion":"failure",
    "head_sha":"abc123",
    "head_branch":"main",
    "updated_at":"2026-08-13T12:01:00Z"
  }
}
            """);

    var candidate = adapter.adapt("workflow_run", payload, fallback).orElseThrow();

    assertThat(candidate.eventType()).isEqualTo(NormalisedEventType.PIPELINE_RUN_COMPLETED);
    assertThat(candidate.pipelineStatus()).isEqualTo(PipelineRunStatus.FAILED);
    assertThat(candidate.externalRunId()).isEqualTo("42");
    assertThat(candidate.runAttempt()).isEqualTo(2);
    assertThat(candidate.sourceFields()).contains("workflow_run.conclusion");
  }

  @Test
  void mapsJenkinsBuildSuccessToSucceededRun() throws Exception {
    JenkinsEventAdapter adapter = new JenkinsEventAdapter();
    var payload =
        objectMapper.readTree(
"""
            {
  "name":"orders",
  "build":{
    "number":7,
    "fullDisplayName":"orders #7",
    "result":"SUCCESS",
    "timestamp":1786622400000
  },
  "scm":{"commitId":"def456","branch":"main"}
}
            """);

    var candidate = adapter.adapt("build", payload, fallback).orElseThrow();

    assertThat(candidate.pipelineStatus()).isEqualTo(PipelineRunStatus.SUCCEEDED);
    assertThat(candidate.eventType()).isEqualTo(NormalisedEventType.PIPELINE_RUN_COMPLETED);
    assertThat(candidate.externalRunId()).isEqualTo("7");
    assertThat(candidate.commitSha()).isEqualTo("def456");
  }

  @Test
  void ignoresUnsupportedGithubEventType() throws Exception {
    GitHubActionsEventAdapter adapter = new GitHubActionsEventAdapter();

    assertThat(adapter.adapt("check_suite", objectMapper.readTree("{}"), fallback)).isEmpty();
  }
}
