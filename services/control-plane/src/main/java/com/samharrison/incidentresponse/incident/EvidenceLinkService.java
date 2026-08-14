package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.evidence.Evidence;
import com.samharrison.incidentresponse.evidence.EvidenceRepository;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEventRepository;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRole;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceLinkService {

  private static final Set<OrganisationMembershipRole> LINK_WRITERS =
      Set.of(
          OrganisationMembershipRole.OWNER,
          OrganisationMembershipRole.ADMIN,
          OrganisationMembershipRole.MEMBER);

  private final EvidenceRepository evidenceRepository;
  private final IncidentRepository incidentRepository;
  private final NormalisedCiEventRepository eventRepository;
  private final IncidentEvidenceLinkRepository incidentLinkRepository;
  private final EvidenceEventLinkRepository eventLinkRepository;
  private final TenantAccessService tenantAccessService;
  private final AuditRecorder auditRecorder;

  public EvidenceLinkService(
      EvidenceRepository evidenceRepository,
      IncidentRepository incidentRepository,
      NormalisedCiEventRepository eventRepository,
      IncidentEvidenceLinkRepository incidentLinkRepository,
      EvidenceEventLinkRepository eventLinkRepository,
      TenantAccessService tenantAccessService,
      AuditRecorder auditRecorder) {
    this.evidenceRepository = evidenceRepository;
    this.incidentRepository = incidentRepository;
    this.eventRepository = eventRepository;
    this.incidentLinkRepository = incidentLinkRepository;
    this.eventLinkRepository = eventLinkRepository;
    this.tenantAccessService = tenantAccessService;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public IncidentEvidenceLink linkToIncident(
      UUID userId, UUID organisationId, UUID projectId, UUID evidenceId, UUID incidentId) {
    tenantAccessService.requireRole(organisationId, userId, LINK_WRITERS);
    Evidence evidence = requireEvidence(organisationId, projectId, evidenceId);
    Incident incident = requireIncident(organisationId, projectId, incidentId);
    IncidentEvidenceLink link =
        incidentLinkRepository.save(
            new IncidentEvidenceLink(UUID.randomUUID(), incident, evidence, Instant.now()));
    auditRecorder.record(
        userId,
        organisationId,
        "EVIDENCE_LINKED_TO_INCIDENT",
        "EVIDENCE",
        evidenceId,
        "{\"incidentId\":\"" + incidentId + "\"}");
    return link;
  }

  @Transactional
  public EvidenceEventLink linkToEvent(
      UUID userId, UUID organisationId, UUID projectId, UUID evidenceId, UUID eventId) {
    tenantAccessService.requireRole(organisationId, userId, LINK_WRITERS);
    Evidence evidence = requireEvidence(organisationId, projectId, evidenceId);
    NormalisedCiEvent event =
        eventRepository
            .findByIdAndOrganisationIdAndProjectId(eventId, organisationId, projectId)
            .orElseThrow(
                () ->
                    new TenantAccessException(
                        HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "The event was not found."));
    EvidenceEventLink link =
        eventLinkRepository.save(
            new EvidenceEventLink(UUID.randomUUID(), evidence, event, Instant.now()));
    auditRecorder.record(
        userId,
        organisationId,
        "EVIDENCE_LINKED_TO_EVENT",
        "EVIDENCE",
        evidenceId,
        "{\"eventId\":\"" + eventId + "\"}");
    return link;
  }

  private Evidence requireEvidence(UUID organisationId, UUID projectId, UUID evidenceId) {
    return evidenceRepository
        .findByIdAndOrganisationIdAndProjectId(evidenceId, organisationId, projectId)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND, "EVIDENCE_NOT_FOUND", "The evidence was not found."));
  }

  private Incident requireIncident(UUID organisationId, UUID projectId, UUID incidentId) {
    return incidentRepository
        .findByIdAndOrganisationIdAndProjectId(incidentId, organisationId, projectId)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND, "INCIDENT_NOT_FOUND", "The incident was not found."));
  }
}
