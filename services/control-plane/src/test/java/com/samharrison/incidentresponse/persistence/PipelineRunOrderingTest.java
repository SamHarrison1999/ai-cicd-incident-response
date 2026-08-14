package com.samharrison.incidentresponse.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.samharrison.incidentresponse.ingestion.EventProvider;
import com.samharrison.incidentresponse.ingestion.EventSource;
import com.samharrison.incidentresponse.ingestion.EventSourceStatus;
import com.samharrison.incidentresponse.ingestion.PipelineRun;
import com.samharrison.incidentresponse.ingestion.PipelineRunStatus;
import com.samharrison.incidentresponse.ingestion.SignatureAlgorithm;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PipelineRunOrderingTest {

  @Test
  void olderEvidenceDoesNotMoveTheProjectionBackwards() {
    Organisation organisation = newOrganisation();
    Project project = newProject(organisation);
    EventSource source = newEventSource(organisation, project);
    Instant completedAt = Instant.parse("2026-08-14T10:00:00Z");
    PipelineRun run = newRun(organisation, project, source, completedAt);

    run.applyStatus(PipelineRunStatus.RUNNING, completedAt.minusSeconds(30));

    assertThat(run.getStatus()).isEqualTo(PipelineRunStatus.FAILED);
    assertThat(run.getLastEventOccurredAt()).isEqualTo(completedAt);
  }

  @Test
  void laterEvidenceCannotRegressACompletedRun() {
    Organisation organisation = newOrganisation();
    Project project = newProject(organisation);
    EventSource source = newEventSource(organisation, project);
    Instant completedAt = Instant.parse("2026-08-14T10:00:00Z");
    PipelineRun run = newRun(organisation, project, source, completedAt);

    assertThatThrownBy(() -> run.applyStatus(PipelineRunStatus.RUNNING, completedAt.plusSeconds(1)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("terminal pipeline runs cannot regress to a non-terminal status");
  }

  private static PipelineRun newRun(
      Organisation organisation, Project project, EventSource source, Instant occurredAt) {
    return new PipelineRun(
        UUID.randomUUID(),
        organisation,
        project,
        source,
        EventProvider.GITHUB_ACTIONS,
        "run-42",
        "continuous-integration",
        1,
        PipelineRunStatus.FAILED,
        "a".repeat(40),
        "refs/heads/main",
        null,
        occurredAt);
  }

  private static Organisation newOrganisation() {
    return new Organisation(UUID.randomUUID(), "Test Organisation", "platform");
  }

  private static Project newProject(Organisation organisation) {
    return new Project(
        UUID.randomUUID(),
        organisation,
        "Test Project",
        "incident-response",
        "Timeline test project",
        ProjectStatus.ACTIVE);
  }

  private static EventSource newEventSource(Organisation organisation, Project project) {
    return new EventSource(
        UUID.randomUUID(),
        organisation,
        project,
        EventProvider.GITHUB_ACTIONS,
        "GitHub Actions",
        EventSourceStatus.ENABLED,
        "local/event-sources/test",
        SignatureAlgorithm.HMAC_SHA256,
        300,
        262_144);
  }
}
