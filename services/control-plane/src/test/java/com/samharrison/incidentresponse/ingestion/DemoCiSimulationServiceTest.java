package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class DemoCiSimulationServiceTest {

  private static final Instant NOW = Instant.parse("2026-09-05T15:00:00Z");

  @Test
  void simulatesFailedRunThroughExistingSignedEventSource() throws Exception {
    EventSourceRepository repository = mock(EventSourceRepository.class);
    EventSourceService eventSourceService = mock(EventSourceService.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    WebhookSecretResolver secretResolver = mock(WebhookSecretResolver.class);
    WebhookSignatureService signatureService = mock(WebhookSignatureService.class);
    WebhookIngestionService ingestionService = mock(WebhookIngestionService.class);

    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID eventSourceId = UUID.randomUUID();
    UUID deliveryId = UUID.randomUUID();

    EventSource source = mock(EventSource.class);
    when(source.getId()).thenReturn(eventSourceId);
    when(source.getSigningSecretReference()).thenReturn("local-simulator");

    when(repository.findByProjectIdAndOrganisationIdAndDisplayName(
            projectId, organisationId, "Browser Demo GitHub Actions"))
        .thenReturn(Optional.of(source));

    byte[] secret = "demo-secret".getBytes(StandardCharsets.UTF_8);
    when(secretResolver.resolve("local-simulator")).thenReturn(Optional.of(secret));

    when(signatureService.calculateSignature(any(), eq("workflow_run"), any(), any(), any()))
        .thenReturn("sha256=" + "a".repeat(64));

    when(ingestionService.ingest(
            eq(eventSourceId),
            any(),
            eq("workflow_run"),
            eq(NOW.toString()),
            eq("sha256=" + "a".repeat(64)),
            eq("application/json"),
            anyLong(),
            any(InputStream.class)))
        .thenReturn(
            new WebhookAcceptanceResponse(deliveryId, false, WebhookDeliveryStatus.RECEIVED, NOW));

    DemoCiSimulationService service =
        new DemoCiSimulationService(
            repository,
            eventSourceService,
            tenantAccessService,
            secretResolver,
            signatureService,
            ingestionService,
            Clock.fixed(NOW, ZoneOffset.UTC));

    DemoCiSimulationService.DemoCiSimulationResult result =
        service.simulate(
            userId, organisationId, projectId, "demo-deployment", "main", DemoCiOutcome.FAILED);

    assertThat(result.eventSourceId()).isEqualTo(eventSourceId);
    assertThat(result.externalRunId()).isEqualTo(Long.toString(NOW.toEpochMilli()));
    assertThat(result.outcome()).isEqualTo(DemoCiOutcome.FAILED);
    assertThat(result.acceptance().deliveryId()).isEqualTo(deliveryId);

    verify(tenantAccessService).requireRole(eq(organisationId), eq(userId), anySet());

    verify(eventSourceService, never())
        .create(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(Integer.class),
            any(Integer.class));

    ArgumentCaptor<InputStream> bodyCaptor = ArgumentCaptor.forClass(InputStream.class);

    verify(ingestionService)
        .ingest(
            eq(eventSourceId),
            any(),
            eq("workflow_run"),
            eq(NOW.toString()),
            eq("sha256=" + "a".repeat(64)),
            eq("application/json"),
            anyLong(),
            bodyCaptor.capture());

    JsonNode payload = new ObjectMapper().readTree(bodyCaptor.getValue().readAllBytes());

    assertThat(payload.path("workflow_run").path("name").asText()).isEqualTo("demo-deployment");
    assertThat(payload.path("workflow_run").path("head_branch").asText()).isEqualTo("main");
    assertThat(payload.path("workflow_run").path("conclusion").asText()).isEqualTo("failure");

    assertThat(secret).containsOnly((byte) 0);
  }

  @Test
  void provisionsSourceAndSimulatesSuccessfulRun() {
    EventSourceRepository repository = mock(EventSourceRepository.class);
    EventSourceService eventSourceService = mock(EventSourceService.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    WebhookSecretResolver secretResolver = mock(WebhookSecretResolver.class);
    WebhookSignatureService signatureService = mock(WebhookSignatureService.class);
    WebhookIngestionService ingestionService = mock(WebhookIngestionService.class);

    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID eventSourceId = UUID.randomUUID();

    EventSource source = mock(EventSource.class);
    when(source.getId()).thenReturn(eventSourceId);
    when(source.getSigningSecretReference()).thenReturn("local-simulator");

    when(repository.findByProjectIdAndOrganisationIdAndDisplayName(
            projectId, organisationId, "Browser Demo GitHub Actions"))
        .thenReturn(Optional.empty());

    when(eventSourceService.create(
            userId,
            organisationId,
            projectId,
            EventProvider.GITHUB_ACTIONS,
            "Browser Demo GitHub Actions",
            EventSourceStatus.ENABLED,
            "local-simulator",
            SignatureAlgorithm.HMAC_SHA256,
            300,
            262144))
        .thenReturn(source);

    when(secretResolver.resolve("local-simulator"))
        .thenReturn(Optional.of("demo-secret".getBytes(StandardCharsets.UTF_8)));

    when(signatureService.calculateSignature(any(), eq("workflow_run"), any(), any(), any()))
        .thenReturn("sha256=" + "b".repeat(64));

    when(ingestionService.ingest(
            eq(eventSourceId),
            any(),
            eq("workflow_run"),
            any(),
            any(),
            eq("application/json"),
            anyLong(),
            any(InputStream.class)))
        .thenReturn(
            new WebhookAcceptanceResponse(
                UUID.randomUUID(), false, WebhookDeliveryStatus.RECEIVED, NOW));

    DemoCiSimulationService service =
        new DemoCiSimulationService(
            repository,
            eventSourceService,
            tenantAccessService,
            secretResolver,
            signatureService,
            ingestionService,
            Clock.fixed(NOW, ZoneOffset.UTC));

    DemoCiSimulationService.DemoCiSimulationResult result =
        service.simulate(
            userId, organisationId, projectId, "release", "feature/demo", DemoCiOutcome.SUCCEEDED);

    assertThat(result.outcome()).isEqualTo(DemoCiOutcome.SUCCEEDED);

    verify(eventSourceService)
        .create(
            userId,
            organisationId,
            projectId,
            EventProvider.GITHUB_ACTIONS,
            "Browser Demo GitHub Actions",
            EventSourceStatus.ENABLED,
            "local-simulator",
            SignatureAlgorithm.HMAC_SHA256,
            300,
            262144);
  }

  @Test
  void rejectsSimulationWhenServerSideSecretIsUnavailable() {
    EventSourceRepository repository = mock(EventSourceRepository.class);
    EventSourceService eventSourceService = mock(EventSourceService.class);
    TenantAccessService tenantAccessService = mock(TenantAccessService.class);
    WebhookSecretResolver secretResolver = mock(WebhookSecretResolver.class);
    WebhookSignatureService signatureService = mock(WebhookSignatureService.class);
    WebhookIngestionService ingestionService = mock(WebhookIngestionService.class);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();

    EventSource source = mock(EventSource.class);
    when(source.getSigningSecretReference()).thenReturn("local-simulator");

    when(repository.findByProjectIdAndOrganisationIdAndDisplayName(
            eq(projectId), eq(organisationId), any()))
        .thenReturn(Optional.of(source));

    when(secretResolver.resolve("local-simulator")).thenReturn(Optional.empty());

    DemoCiSimulationService service =
        new DemoCiSimulationService(
            repository,
            eventSourceService,
            tenantAccessService,
            secretResolver,
            signatureService,
            ingestionService,
            Clock.fixed(NOW, ZoneOffset.UTC));

    assertThatThrownBy(
            () ->
                service.simulate(
                    UUID.randomUUID(),
                    organisationId,
                    projectId,
                    "demo-deployment",
                    "main",
                    DemoCiOutcome.FAILED))
        .isInstanceOf(WebhookIngestionException.class)
        .hasMessage("The event source is temporarily unavailable.");

    verify(ingestionService, never())
        .ingest(any(), any(), any(), any(), any(), any(), anyLong(), any(InputStream.class));
  }
}
