package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IncidentStateTransitionTest {

  @Test
  void followsTheDocumentedLifecycleAndPreservesIdentityWhenReopened() {
    Incident incident = newIncident();
    Instant triagedAt = Instant.parse("2026-08-14T11:01:00Z");
    Instant resolvedAt = Instant.parse("2026-08-14T11:15:00Z");

    incident.transitionTo(IncidentStatus.TRIAGED, triagedAt);
    incident.transitionTo(IncidentStatus.RESOLVED, resolvedAt);
    incident.transitionTo(IncidentStatus.REOPENED, resolvedAt.plusSeconds(60));

    assertThat(incident.getStatus()).isEqualTo(IncidentStatus.REOPENED);
    assertThat(incident.getId()).isNotNull();
    assertThat(incident.getResolvedAt()).isNull();
  }

  @Test
  void rejectsAnInvalidTransitionWithoutChangingState() {
    Incident incident = newIncident();

    assertThatThrownBy(
            () ->
                incident.transitionTo(
                    IncidentStatus.MONITORING, Instant.parse("2026-08-14T11:01:00Z")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("incident transition is not allowed: DETECTED -> MONITORING");

    assertThat(incident.getStatus()).isEqualTo(IncidentStatus.DETECTED);
  }

  private static Incident newIncident() {
    Organisation organisation =
        new Organisation(UUID.randomUUID(), "Test Organisation", "platform");
    Project project =
        new Project(
            UUID.randomUUID(),
            organisation,
            "Test Project",
            "incident-response",
            "Incident persistence test project",
            ProjectStatus.ACTIVE);
    return new Incident(
        UUID.randomUUID(),
        organisation,
        project,
        "Pipeline failure",
        "A bounded synthetic failure summary.",
        Instant.parse("2026-08-14T11:00:00Z"));
  }
}
