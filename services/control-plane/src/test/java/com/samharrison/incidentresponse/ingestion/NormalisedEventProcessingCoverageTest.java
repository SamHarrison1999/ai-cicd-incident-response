package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.samharrison.incidentresponse.incident.IncidentCorrelationWorkflow;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NormalisedEventProcessingCoverageTest {

  @Test
  void rejectsPayloadThatCannotBeParsed() {
    NormalisedEventProcessingService service = service();

    assertThatThrownBy(
            () ->
                service.process(
                    mock(EventSource.class),
                    mock(WebhookDelivery.class),
                    "not-json".getBytes(),
                    Instant.parse("2026-08-16T10:00:00Z")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("A previously validated webhook could not be parsed.");
  }

  @Test
  void marksUnsupportedEventsAsProcessed() {
    ProviderEventAdapterRegistry adapters = mock(ProviderEventAdapterRegistry.class);
    PipelineRunRepository pipelineRuns = mock(PipelineRunRepository.class);
    NormalisedCiEventRepository events = mock(NormalisedCiEventRepository.class);
    WebhookDeliveryRepository deliveries = mock(WebhookDeliveryRepository.class);
    IngestionMetrics metrics = mock(IngestionMetrics.class);
    EventSource source = mock(EventSource.class);
    WebhookDelivery delivery = mock(WebhookDelivery.class);
    when(source.getProvider()).thenReturn(EventProvider.GITHUB_ACTIONS);
    when(adapters.find(EventProvider.GITHUB_ACTIONS)).thenReturn(Optional.empty());

    new NormalisedEventProcessingService(
            adapters,
            pipelineRuns,
            events,
            deliveries,
            metrics,
            mock(IncidentCorrelationWorkflow.class))
        .process(source, delivery, "{}".getBytes(), Instant.parse("2026-08-16T10:00:00Z"));

    org.mockito.Mockito.verify(delivery)
        .markProcessed(eq("UNSUPPORTED_PROVIDER_EVENT"), any(Instant.class));
    org.mockito.Mockito.verify(deliveries).save(delivery);
    org.mockito.Mockito.verify(metrics).recordUnsupportedEvent();
  }

  @Test
  void updatesAnExistingPipelineRunWhenTheEventIsNewer() {
    ProviderEventAdapterRegistry adapters = mock(ProviderEventAdapterRegistry.class);
    ProviderEventAdapter adapter = mock(ProviderEventAdapter.class);
    PipelineRunRepository pipelineRuns = mock(PipelineRunRepository.class);
    NormalisedCiEventRepository events = mock(NormalisedCiEventRepository.class);
    WebhookDeliveryRepository deliveries = mock(WebhookDeliveryRepository.class);
    IngestionMetrics metrics = mock(IngestionMetrics.class);
    EventSource source = mock(EventSource.class);
    WebhookDelivery delivery = mock(WebhookDelivery.class);
    PipelineRun run = mock(PipelineRun.class);
    Organisation organisation = mock(Organisation.class);
    Project project = mock(Project.class);
    UUID runId = UUID.randomUUID();
    Instant previous = Instant.parse("2026-08-16T09:00:00Z");
    Instant occurred = Instant.parse("2026-08-16T10:00:00Z");
    NormalisedEventCandidate candidate =
        new NormalisedEventCandidate(
            NormalisedEventType.PIPELINE_RUN_COMPLETED,
            occurred,
            "run-1",
            "build",
            1,
            PipelineRunStatus.FAILED,
            "abc123",
            "main",
            "production",
            "failure",
            List.of("workflow_run.id"));

    when(source.getProvider()).thenReturn(EventProvider.GITHUB_ACTIONS);
    when(source.getId()).thenReturn(UUID.randomUUID());
    when(source.getOrganisation()).thenReturn(organisation);
    when(source.getProject()).thenReturn(project);
    when(delivery.getProviderEventType()).thenReturn("workflow_run");
    when(adapters.find(EventProvider.GITHUB_ACTIONS)).thenReturn(Optional.of(adapter));
    when(adapter.adapt(eq("workflow_run"), any(JsonNode.class), eq(occurred)))
        .thenReturn(Optional.of(candidate));
    when(pipelineRuns.findByEventSourceIdAndExternalRunIdAndAttempt(any(), eq("run-1"), eq(1)))
        .thenReturn(Optional.of(run));
    when(run.getId()).thenReturn(runId);
    when(run.getLastEventOccurredAt()).thenReturn(previous);
    when(pipelineRuns.existsById(runId)).thenReturn(true);
    when(pipelineRuns.save(run)).thenReturn(run);

    NormalisedEventProcessingService service =
        new NormalisedEventProcessingService(
            adapters,
            pipelineRuns,
            events,
            deliveries,
            metrics,
            mock(IncidentCorrelationWorkflow.class));
    service.process(source, delivery, "{}".getBytes(), occurred);

    org.mockito.Mockito.verify(run).applyStatus(PipelineRunStatus.FAILED, occurred);
    org.mockito.Mockito.verify(events).save(any(NormalisedCiEvent.class));
    org.mockito.Mockito.verify(metrics).recordNormalisedEvent();

    when(run.getLastEventOccurredAt()).thenReturn(occurred);
    service.process(source, delivery, "{}".getBytes(), occurred);
    org.mockito.Mockito.verify(run, org.mockito.Mockito.times(1))
        .applyStatus(PipelineRunStatus.FAILED, occurred);

    when(pipelineRuns.existsById(runId)).thenReturn(false);
    service.process(source, delivery, "{}".getBytes(), occurred);

    when(run.getId()).thenReturn(null);
    service.process(source, delivery, "{}".getBytes(), occurred);
    org.mockito.Mockito.verify(run, org.mockito.Mockito.times(1))
        .applyStatus(PipelineRunStatus.FAILED, occurred);
  }

  private static NormalisedEventProcessingService service() {
    return new NormalisedEventProcessingService(
        mock(ProviderEventAdapterRegistry.class),
        mock(PipelineRunRepository.class),
        mock(NormalisedCiEventRepository.class),
        mock(WebhookDeliveryRepository.class),
        mock(IngestionMetrics.class),
        mock(IncidentCorrelationWorkflow.class));
  }
}
