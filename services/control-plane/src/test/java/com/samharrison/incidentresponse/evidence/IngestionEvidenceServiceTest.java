package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.incident.EvidenceEventLink;
import com.samharrison.incidentresponse.incident.EvidenceEventLinkRepository;
import com.samharrison.incidentresponse.incident.Incident;
import com.samharrison.incidentresponse.incident.IncidentEvidenceLink;
import com.samharrison.incidentresponse.incident.IncidentEvidenceLinkRepository;
import com.samharrison.incidentresponse.ingestion.EventProvider;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import com.samharrison.incidentresponse.ingestion.NormalisedEventType;
import com.samharrison.incidentresponse.ingestion.PipelineRunStatus;
import com.samharrison.incidentresponse.ingestion.WebhookDelivery;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IngestionEvidenceServiceTest {

  @Test
  void captureStoresSanitisedEvidenceAndLinksToEventAndIncident() {
    EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    EvidenceEventLinkRepository eventLinkRepository = mock(EvidenceEventLinkRepository.class);
    IncidentEvidenceLinkRepository incidentLinkRepository =
        mock(IncidentEvidenceLinkRepository.class);
    AuditRecorder auditRecorder = mock(AuditRecorder.class);

    Organisation organisation = mock(Organisation.class);
    Project project = mock(Project.class);
    NormalisedCiEvent event = mock(NormalisedCiEvent.class);
    WebhookDelivery delivery = mock(WebhookDelivery.class);
    Incident incident = mock(Incident.class);

    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID eventId = UUID.randomUUID();

    Instant occurredAt = Instant.parse("2026-09-05T10:00:00Z");
    Instant receivedAt = Instant.parse("2026-09-05T10:00:01Z");

    when(organisation.getId()).thenReturn(organisationId);
    when(project.getId()).thenReturn(projectId);

    when(event.getId()).thenReturn(eventId);
    when(event.getOrganisation()).thenReturn(organisation);
    when(event.getProject()).thenReturn(project);
    when(event.getProvider()).thenReturn(EventProvider.GITHUB_ACTIONS);
    when(event.getEventType()).thenReturn(NormalisedEventType.PIPELINE_RUN_COMPLETED);
    when(event.getPipelineStatus()).thenReturn(PipelineRunStatus.FAILED);
    when(event.getExternalRunId()).thenReturn("9001");
    when(event.getPipelineName()).thenReturn("demo-deployment");
    when(event.getRunAttempt()).thenReturn(1);
    when(event.getCommitSha()).thenReturn("0123456789abcdef0123456789abcdef01234567");
    when(event.getGitRef()).thenReturn("main");
    when(event.getEnvironmentName()).thenReturn("production");
    when(event.getEvidenceSummary()).thenReturn("GitHub Actions workflow failed.");
    when(event.getSourceFields()).thenReturn(List.of("workflow_run.id"));
    when(event.getOccurredAt()).thenReturn(occurredAt);

    when(delivery.getProviderDeliveryId()).thenReturn("delivery-1");
    when(delivery.getPayloadSha256()).thenReturn("a".repeat(64));

    when(incident.getOrganisation()).thenReturn(organisation);
    when(incident.getProject()).thenReturn(project);

    when(evidenceRepository.save(any(Evidence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    IngestionEvidenceService service =
        new IngestionEvidenceService(
            evidenceRepository, eventLinkRepository, incidentLinkRepository, auditRecorder);

    Evidence captured = service.capture(event, Optional.of(incident), delivery, receivedAt);

    assertThat(captured.getKind()).isEqualTo(EvidenceKind.EVENT_SNAPSHOT);
    assertThat(captured.getRetentionClass()).isEqualTo(RetentionClass.STANDARD);
    assertThat(captured.getSourceSystem()).isEqualTo("GITHUB_ACTIONS");
    assertThat(captured.getSourceReference()).isEqualTo("delivery-1");
    assertThat(captured.getContent())
        .contains("eventId=" + eventId)
        .contains("pipelineStatus=FAILED")
        .contains("commitSha=0123456789abcdef0123456789abcdef01234567")
        .contains("gitRef=main")
        .contains("environment=production")
        .contains("payloadSha256=" + "a".repeat(64))
        .contains("summary=GitHub Actions workflow failed.")
        .doesNotContain("payload=");

    verify(eventLinkRepository).save(any(EvidenceEventLink.class));
    verify(incidentLinkRepository).save(any(IncidentEvidenceLink.class));

    verify(auditRecorder)
        .record(
            isNull(),
            eq(organisationId),
            eq("EVIDENCE_INGESTED"),
            eq("EVIDENCE"),
            eq(captured.getId()),
            anyString());
  }

  @Test
  void captureWithoutIncidentLinksOnlyToEvent() {
    EvidenceRepository evidenceRepository = mock(EvidenceRepository.class);
    EvidenceEventLinkRepository eventLinkRepository = mock(EvidenceEventLinkRepository.class);
    IncidentEvidenceLinkRepository incidentLinkRepository =
        mock(IncidentEvidenceLinkRepository.class);
    AuditRecorder auditRecorder = mock(AuditRecorder.class);

    Organisation organisation = mock(Organisation.class);
    Project project = mock(Project.class);
    NormalisedCiEvent event = mock(NormalisedCiEvent.class);
    WebhookDelivery delivery = mock(WebhookDelivery.class);

    UUID organisationId = UUID.randomUUID();

    when(organisation.getId()).thenReturn(organisationId);

    when(event.getId()).thenReturn(UUID.randomUUID());
    when(event.getOrganisation()).thenReturn(organisation);
    when(event.getProject()).thenReturn(project);
    when(event.getProvider()).thenReturn(EventProvider.GITHUB_ACTIONS);
    when(event.getEventType()).thenReturn(NormalisedEventType.PIPELINE_RUN_COMPLETED);
    when(event.getPipelineStatus()).thenReturn(PipelineRunStatus.SUCCEEDED);
    when(event.getExternalRunId()).thenReturn("9002");
    when(event.getPipelineName()).thenReturn("demo-deployment");
    when(event.getRunAttempt()).thenReturn(1);
    when(event.getEvidenceSummary()).thenReturn("GitHub Actions workflow succeeded.");
    when(event.getSourceFields()).thenReturn(List.of("workflow_run.id"));
    when(event.getOccurredAt()).thenReturn(Instant.parse("2026-09-05T11:00:00Z"));

    when(delivery.getProviderDeliveryId()).thenReturn("delivery-2");
    when(delivery.getPayloadSha256()).thenReturn("b".repeat(64));

    when(evidenceRepository.save(any(Evidence.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    IngestionEvidenceService service =
        new IngestionEvidenceService(
            evidenceRepository, eventLinkRepository, incidentLinkRepository, auditRecorder);

    Evidence captured =
        service.capture(event, Optional.empty(), delivery, Instant.parse("2026-09-05T11:00:01Z"));

    assertThat(captured.getKind()).isEqualTo(EvidenceKind.EVENT_SNAPSHOT);

    verify(eventLinkRepository).save(any(EvidenceEventLink.class));
    verify(incidentLinkRepository, never()).save(any(IncidentEvidenceLink.class));
  }
}
