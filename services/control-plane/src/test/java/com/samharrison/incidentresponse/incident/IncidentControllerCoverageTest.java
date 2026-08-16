package com.samharrison.incidentresponse.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class IncidentControllerCoverageTest {
  @Test
  void transitionsWithExplicitAndDefaultTimestamps() {
    IncidentService service = mock(IncidentService.class);
    CurrentUserProvider users = mock(CurrentUserProvider.class);
    Authentication authentication = mock(Authentication.class);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID incidentId = UUID.randomUUID();
    Incident incident = mock(Incident.class);
    when(users.requireUserId(authentication)).thenReturn(userId);
    when(incident.getId()).thenReturn(incidentId);
    when(incident.getStatus()).thenReturn(IncidentStatus.TRIAGED);
    when(incident.getTitle()).thenReturn("Incident");
    when(incident.getSummary()).thenReturn("Summary");
    when(incident.getDetectedAt()).thenReturn(Instant.EPOCH);
    when(service.transition(any(), any(), any(), any(), any(), any())).thenReturn(incident);
    IncidentController controller = new IncidentController(service, users);

    IncidentController.IncidentResponse explicitTimestampResponse =
        controller.transition(
            authentication,
            organisationId,
            projectId,
            incidentId,
            new IncidentController.IncidentStatusRequest(IncidentStatus.TRIAGED, Instant.EPOCH));
    assertThat(explicitTimestampResponse.status()).isEqualTo("TRIAGED");

    IncidentController.IncidentResponse defaultTimestampResponse =
        controller.transition(
            authentication,
            organisationId,
            projectId,
            incidentId,
            new IncidentController.IncidentStatusRequest(IncidentStatus.TRIAGED, null));
    assertThat(defaultTimestampResponse.id()).isEqualTo(incidentId);
  }
}
