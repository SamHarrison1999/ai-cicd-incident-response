package com.samharrison.incidentresponse.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.samharrison.incidentresponse.ingestion.EventProvider;
import com.samharrison.incidentresponse.ingestion.EventSource;
import com.samharrison.incidentresponse.ingestion.EventSourceStatus;
import com.samharrison.incidentresponse.ingestion.PipelineRun;
import com.samharrison.incidentresponse.ingestion.PipelineRunStatus;
import com.samharrison.incidentresponse.ingestion.SignatureAlgorithm;
import com.samharrison.incidentresponse.ingestion.WebhookDelivery;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventIngestionDomainModelTest {

  @Test
  void eventSourceRejectsBlankSecretReference() {
    Organisation organisation = newOrganisation("platform");
    Project project = newProject(organisation, "incident-response");

    assertThatThrownBy(
            () ->
                new EventSource(
                    UUID.randomUUID(),
                    organisation,
                    project,
                    EventProvider.GITHUB_ACTIONS,
                    "GitHub Actions",
                    EventSourceStatus.ENABLED,
                    " ",
                    SignatureAlgorithm.HMAC_SHA256,
                    300,
                    262_144))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("signingSecretReference must not be blank");
  }

  @Test
  void webhookDeliveryRejectsNonSha256PayloadHash() {
    Organisation organisation = newOrganisation("platform");
    Project project = newProject(organisation, "incident-response");
    EventSource eventSource = newEventSource(organisation, project, "GitHub Actions");

    assertThatThrownBy(
            () ->
                new WebhookDelivery(
                    UUID.randomUUID(),
                    organisation,
                    project,
                    eventSource,
                    "delivery-1",
                    "workflow_run",
                    "not-a-sha256-hash",
                    Instant.now(),
                    Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("payloadSha256 must be 64 lowercase hexadecimal characters");
  }

  @Test
  void terminalPipelineRunCannotRegressToRunning() {
    Organisation organisation = newOrganisation("platform");
    Project project = newProject(organisation, "incident-response");
    EventSource eventSource = newEventSource(organisation, project, "GitHub Actions");
    Instant failedAt = Instant.parse("2026-08-13T10:00:00Z");
    PipelineRun run =
        new PipelineRun(
            UUID.randomUUID(),
            organisation,
            project,
            eventSource,
            EventProvider.GITHUB_ACTIONS,
            "run-42",
            "continuous-integration",
            1,
            PipelineRunStatus.FAILED,
            "a".repeat(40),
            "refs/heads/main",
            null,
            failedAt);

    assertThatThrownBy(() -> run.applyStatus(PipelineRunStatus.RUNNING, failedAt.plusSeconds(1)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("terminal pipeline runs cannot regress to a non-terminal status");
  }

  private Organisation newOrganisation(String slug) {
    return new Organisation(UUID.randomUUID(), "Test Organisation", slug);
  }

  private Project newProject(Organisation organisation, String slug) {
    return new Project(
        UUID.randomUUID(),
        organisation,
        "Test Project",
        slug,
        "Domain model test",
        ProjectStatus.ACTIVE);
  }

  private EventSource newEventSource(
      Organisation organisation, Project project, String displayName) {
    return new EventSource(
        UUID.randomUUID(),
        organisation,
        project,
        EventProvider.GITHUB_ACTIONS,
        displayName,
        EventSourceStatus.ENABLED,
        "local/event-sources/test",
        SignatureAlgorithm.HMAC_SHA256,
        300,
        262_144);
  }
}
