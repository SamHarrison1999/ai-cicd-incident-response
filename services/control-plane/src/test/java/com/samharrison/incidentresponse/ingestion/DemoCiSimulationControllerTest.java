package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

class DemoCiSimulationControllerTest {

  @Test
  void returnsCreatedSimulationMetadata() {
    DemoCiSimulationService service = mock(DemoCiSimulationService.class);
    CurrentUserProvider users = mock(CurrentUserProvider.class);
    Authentication authentication = mock(Authentication.class);

    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID eventSourceId = UUID.randomUUID();
    UUID deliveryId = UUID.randomUUID();
    Instant receivedAt = Instant.parse("2026-09-05T15:00:00Z");

    when(users.requireUserId(authentication)).thenReturn(userId);

    when(service.simulate(
            userId, organisationId, projectId, "demo-deployment", "main", DemoCiOutcome.FAILED))
        .thenReturn(
            new DemoCiSimulationService.DemoCiSimulationResult(
                eventSourceId,
                "provider-delivery",
                "9001",
                "demo-deployment",
                "main",
                DemoCiOutcome.FAILED,
                new WebhookAcceptanceResponse(
                    deliveryId, false, WebhookDeliveryStatus.RECEIVED, receivedAt)));

    DemoCiSimulationController controller = new DemoCiSimulationController(service, users);

    var response =
        controller.simulate(
            authentication,
            organisationId,
            projectId,
            new DemoCiSimulationController.DemoCiSimulationRequest(
                "demo-deployment", "main", DemoCiOutcome.FAILED));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().eventSourceId()).isEqualTo(eventSourceId);
    assertThat(response.getBody().pipelineName()).isEqualTo("demo-deployment");
    assertThat(response.getBody().outcome()).isEqualTo(DemoCiOutcome.FAILED);
    assertThat(response.getBody().deliveryId()).isEqualTo(deliveryId);
    assertThat(response.getBody().duplicate()).isFalse();
    assertThat(response.getBody().deliveryStatus()).isEqualTo("RECEIVED");
    assertThat(response.getBody().receivedAt()).isEqualTo(receivedAt);
  }
}
