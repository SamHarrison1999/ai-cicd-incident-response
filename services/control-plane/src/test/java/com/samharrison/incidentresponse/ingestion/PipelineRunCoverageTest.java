package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PipelineRunCoverageTest {

  @Test
  void appliesRunningAndTerminalStatusesInOrder() {
    Instant first = Instant.parse("2026-08-16T10:00:00Z");
    Instant second = Instant.parse("2026-08-16T10:01:00Z");
    PipelineRun run = newRun(PipelineRunStatus.QUEUED, first);

    run.applyStatus(PipelineRunStatus.RUNNING, second);
    run.applyStatus(PipelineRunStatus.SUCCEEDED, second.plusSeconds(60));

    assertThat(run.getStatus()).isEqualTo(PipelineRunStatus.SUCCEEDED);
    assertThat(run.getStartedAt()).isEqualTo(second);
    assertThat(run.getCompletedAt()).isEqualTo(second.plusSeconds(60));
  }

  @Test
  void ignoresOlderEventsAndRejectsTerminalRegression() {
    Instant first = Instant.parse("2026-08-16T10:00:00Z");
    PipelineRun run = newRun(PipelineRunStatus.FAILED, first);

    run.applyStatus(PipelineRunStatus.RUNNING, first.minusSeconds(1));
    assertThat(run.getStatus()).isEqualTo(PipelineRunStatus.FAILED);
    assertThatThrownBy(() -> run.applyStatus(PipelineRunStatus.RUNNING, first.plusSeconds(1)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("terminal pipeline runs cannot regress to a non-terminal status");
  }

  @Test
  void rejectsInvalidIdentityFields() {
    assertThatThrownBy(() -> newRun(PipelineRunStatus.QUEUED, Instant.now(), "", 1))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> newRun(PipelineRunStatus.QUEUED, Instant.now(), "run", 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void initialisesStartedAtForRunsAlreadyInProgress() {
    Instant occurredAt = Instant.parse("2026-08-16T10:00:00Z");
    PipelineRun run = newRun(PipelineRunStatus.RUNNING, occurredAt);

    assertThat(run.getStartedAt()).isEqualTo(occurredAt);
    run.applyStatus(PipelineRunStatus.RUNNING, occurredAt.plusSeconds(1));
    assertThat(run.getStartedAt()).isEqualTo(occurredAt);
  }

  private static PipelineRun newRun(PipelineRunStatus status, Instant occurredAt) {
    return newRun(status, occurredAt, "run-1", 1);
  }

  private static PipelineRun newRun(
      PipelineRunStatus status, Instant occurredAt, String externalRunId, int attempt) {
    return new PipelineRun(
        UUID.randomUUID(),
        mock(Organisation.class),
        mock(Project.class),
        mock(EventSource.class),
        EventProvider.GITHUB_ACTIONS,
        externalRunId,
        "build",
        attempt,
        status,
        "abc123",
        "main",
        "production",
        occurredAt);
  }
}
