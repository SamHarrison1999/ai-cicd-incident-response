package com.samharrison.incidentresponse.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samharrison.incidentresponse.evidence.IngestionEvidenceService;
import com.samharrison.incidentresponse.incident.Incident;
import com.samharrison.incidentresponse.incident.IncidentCorrelationWorkflow;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class NormalisedEventProcessingService {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ProviderEventAdapterRegistry adapterRegistry;
  private final PipelineRunRepository pipelineRunRepository;
  private final NormalisedCiEventRepository normalisedCiEventRepository;
  private final WebhookDeliveryRepository webhookDeliveryRepository;
  private final IngestionMetrics ingestionMetrics;
  private final IncidentCorrelationWorkflow incidentCorrelationWorkflow;
  private final IngestionEvidenceService ingestionEvidenceService;

  NormalisedEventProcessingService(
      ProviderEventAdapterRegistry adapterRegistry,
      PipelineRunRepository pipelineRunRepository,
      NormalisedCiEventRepository normalisedCiEventRepository,
      WebhookDeliveryRepository webhookDeliveryRepository,
      IngestionMetrics ingestionMetrics,
      IncidentCorrelationWorkflow incidentCorrelationWorkflow,
      IngestionEvidenceService ingestionEvidenceService) {
    this.adapterRegistry = adapterRegistry;
    this.pipelineRunRepository = pipelineRunRepository;
    this.normalisedCiEventRepository = normalisedCiEventRepository;
    this.webhookDeliveryRepository = webhookDeliveryRepository;
    this.ingestionMetrics = ingestionMetrics;
    this.incidentCorrelationWorkflow = incidentCorrelationWorkflow;
    this.ingestionEvidenceService = ingestionEvidenceService;
  }

  @Transactional
  void process(
      EventSource eventSource, WebhookDelivery delivery, byte[] payload, Instant receivedAt) {
    JsonNode payloadTree;
    try {
      payloadTree = objectMapper.readTree(payload);
    } catch (IOException exception) {
      throw new IllegalStateException(
          "A previously validated webhook could not be parsed.", exception);
    }

    Optional<ProviderEventAdapter> adapter = adapterRegistry.find(eventSource.getProvider());
    Optional<NormalisedEventCandidate> candidate =
        adapter.flatMap(
            value -> value.adapt(delivery.getProviderEventType(), payloadTree, receivedAt));

    if (candidate.isEmpty()) {
      delivery.markProcessed("UNSUPPORTED_PROVIDER_EVENT", receivedAt);
      ingestionMetrics.recordUnsupportedEvent();
      webhookDeliveryRepository.save(delivery);
      return;
    }

    NormalisedEventCandidate mapped = candidate.get();
    PipelineRun pipelineRun =
        pipelineRunRepository
            .findByEventSourceIdAndExternalRunIdAndAttempt(
                eventSource.getId(), mapped.externalRunId(), mapped.runAttempt())
            .orElseGet(
                () ->
                    new PipelineRun(
                        java.util.UUID.randomUUID(),
                        eventSource.getOrganisation(),
                        eventSource.getProject(),
                        eventSource,
                        eventSource.getProvider(),
                        mapped.externalRunId(),
                        mapped.pipelineName(),
                        mapped.runAttempt(),
                        mapped.pipelineStatus(),
                        mapped.commitSha(),
                        mapped.gitRef(),
                        mapped.environmentName(),
                        mapped.occurredAt()));

    if (pipelineRun.getId() != null
        && pipelineRunRepository.existsById(pipelineRun.getId())
        && !java.util.Objects.equals(pipelineRun.getLastEventOccurredAt(), mapped.occurredAt())) {
      pipelineRun.applyStatus(mapped.pipelineStatus(), mapped.occurredAt());
    }
    pipelineRun = pipelineRunRepository.save(pipelineRun);

    NormalisedCiEvent normalisedEvent =
        new NormalisedCiEvent(
            java.util.UUID.randomUUID(),
            eventSource.getOrganisation(),
            eventSource.getProject(),
            eventSource,
            delivery,
            pipelineRun,
            "v1",
            eventSource.getProvider(),
            mapped.eventType(),
            mapped.occurredAt(),
            receivedAt,
            mapped.externalRunId(),
            mapped.pipelineName(),
            mapped.runAttempt(),
            mapped.pipelineStatus(),
            mapped.commitSha(),
            mapped.gitRef(),
            mapped.environmentName(),
            mapped.evidenceSummary(),
            mapped.sourceFields());

    normalisedCiEventRepository.save(normalisedEvent);
    Optional<Incident> incident = incidentCorrelationWorkflow.correlate(normalisedEvent);
    ingestionEvidenceService.capture(normalisedEvent, incident, delivery, receivedAt);

    delivery.markProcessed("NORMALISED_EVENT_CREATED", receivedAt);
    ingestionMetrics.recordNormalisedEvent();
    webhookDeliveryRepository.save(delivery);
  }
}
