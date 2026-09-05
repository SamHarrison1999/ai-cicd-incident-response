package com.samharrison.incidentresponse.evidence;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.incident.EvidenceEventLink;
import com.samharrison.incidentresponse.incident.EvidenceEventLinkRepository;
import com.samharrison.incidentresponse.incident.Incident;
import com.samharrison.incidentresponse.incident.IncidentEvidenceLink;
import com.samharrison.incidentresponse.incident.IncidentEvidenceLinkRepository;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import com.samharrison.incidentresponse.ingestion.WebhookDelivery;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionEvidenceService {

  private final EvidenceRepository evidenceRepository;
  private final EvidenceEventLinkRepository eventLinkRepository;
  private final IncidentEvidenceLinkRepository incidentLinkRepository;
  private final AuditRecorder auditRecorder;

  public IngestionEvidenceService(
      EvidenceRepository evidenceRepository,
      EvidenceEventLinkRepository eventLinkRepository,
      IncidentEvidenceLinkRepository incidentLinkRepository,
      AuditRecorder auditRecorder) {
    this.evidenceRepository = evidenceRepository;
    this.eventLinkRepository = eventLinkRepository;
    this.incidentLinkRepository = incidentLinkRepository;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public Evidence capture(
      NormalisedCiEvent event,
      Optional<Incident> incident,
      WebhookDelivery delivery,
      Instant receivedAt) {
    String rawContent =
        "eventId="
            + event.getId()
            + "\nprovider="
            + event.getProvider().name()
            + "\neventType="
            + event.getEventType().name()
            + "\npipelineStatus="
            + event.getPipelineStatus().name()
            + "\nexternalRunId="
            + event.getExternalRunId()
            + "\npipelineName="
            + event.getPipelineName()
            + "\nrunAttempt="
            + event.getRunAttempt()
            + "\ncommitSha="
            + event.getCommitSha()
            + "\ngitRef="
            + event.getGitRef()
            + "\nenvironment="
            + event.getEnvironmentName()
            + "\nproviderDeliveryId="
            + delivery.getProviderDeliveryId()
            + "\npayloadSha256="
            + delivery.getPayloadSha256()
            + "\nsummary="
            + event.getEvidenceSummary()
            + "\nsourceFields="
            + String.join(",", event.getSourceFields());

    SanitisedEvidence sanitised = EvidenceSanitiser.sanitise(rawContent);
    String contentHash = EvidenceContentHasher.sha256Hex(sanitised.content());

    Evidence evidence =
        evidenceRepository.save(
            new Evidence(
                UUID.randomUUID(),
                event.getOrganisation(),
                event.getProject(),
                EvidenceKind.EVENT_SNAPSHOT,
                RetentionClass.STANDARD,
                event.getProvider().name(),
                delivery.getProviderDeliveryId(),
                event.getOccurredAt(),
                receivedAt,
                contentHash,
                sanitised.content(),
                sanitised.lineCount()));

    eventLinkRepository.save(new EvidenceEventLink(UUID.randomUUID(), evidence, event, receivedAt));

    incident.ifPresent(
        value ->
            incidentLinkRepository.save(
                new IncidentEvidenceLink(UUID.randomUUID(), value, evidence, receivedAt)));

    auditRecorder.record(
        null,
        event.getOrganisation().getId(),
        "EVIDENCE_INGESTED",
        "EVIDENCE",
        evidence.getId(),
        "{\"eventId\":\""
            + event.getId()
            + "\",\"linkedToIncident\":"
            + incident.isPresent()
            + "}");

    return evidence;
  }
}
