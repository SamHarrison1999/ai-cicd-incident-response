package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class NormalisedEventCandidateCoverageTest {

  @Test
  void copiesValidSourceFields() {
    NormalisedEventCandidate candidate =
        new NormalisedEventCandidate(
            NormalisedEventType.PIPELINE_RUN_COMPLETED,
            Instant.parse("2026-08-16T10:00:00Z"),
            "run-1",
            "build",
            1,
            PipelineRunStatus.SUCCEEDED,
            "abc123",
            "main",
            "production",
            "bounded evidence",
            List.of("workflow_run.id"));

    assertThat(candidate.sourceFields()).containsExactly("workflow_run.id");
  }

  @Test
  void rejectsMissingOrUnboundedFields() {
    Instant now = Instant.now();
    assertThatThrownBy(() -> candidate(null, "build", 1, "evidence", List.of("source")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate("run", " ", 1, "evidence", List.of("source")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate("run", "build", 0, "evidence", List.of("source")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate("run", "build", 1, " ", List.of("source")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate("run", "build", 1, "evidence", List.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate("run", null, 1, "evidence", List.of("source")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> candidate("run", "build", 1, null, List.of("source")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new NormalisedEventCandidate(
                    NormalisedEventType.PIPELINE_RUN_COMPLETED,
                    now,
                    "run",
                    "build",
                    1,
                    PipelineRunStatus.SUCCEEDED,
                    null,
                    null,
                    null,
                    "evidence",
                    null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static NormalisedEventCandidate candidate(
      String run, String name, int attempt, String evidence, List<String> fields) {
    return new NormalisedEventCandidate(
        NormalisedEventType.PIPELINE_RUN_COMPLETED,
        Instant.now(),
        run,
        name,
        attempt,
        PipelineRunStatus.SUCCEEDED,
        null,
        null,
        null,
        evidence,
        fields);
  }
}
