package com.samharrison.incidentresponse.evidence;

import com.samharrison.incidentresponse.incident.EvidenceEventLink;
import com.samharrison.incidentresponse.incident.EvidenceEventLinkRepository;
import com.samharrison.incidentresponse.incident.IncidentEvidenceLink;
import com.samharrison.incidentresponse.incident.IncidentEvidenceLinkRepository;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceViewerService {

  private final EvidenceRepository evidenceRepository;
  private final ProjectRepository projectRepository;
  private final IncidentEvidenceLinkRepository incidentLinkRepository;
  private final EvidenceEventLinkRepository eventLinkRepository;
  private final TenantAccessService tenantAccessService;

  public EvidenceViewerService(
      EvidenceRepository evidenceRepository,
      ProjectRepository projectRepository,
      IncidentEvidenceLinkRepository incidentLinkRepository,
      EvidenceEventLinkRepository eventLinkRepository,
      TenantAccessService tenantAccessService) {
    this.evidenceRepository = evidenceRepository;
    this.projectRepository = projectRepository;
    this.incidentLinkRepository = incidentLinkRepository;
    this.eventLinkRepository = eventLinkRepository;
    this.tenantAccessService = tenantAccessService;
  }

  @Transactional(readOnly = true)
  public EvidenceView get(UUID userId, UUID organisationId, UUID projectId, UUID evidenceId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    requireProject(organisationId, projectId);
    Evidence evidence =
        evidenceRepository
            .findByIdAndOrganisationIdAndProjectId(evidenceId, organisationId, projectId)
            .orElseThrow(
                () ->
                    new TenantAccessException(
                        HttpStatus.NOT_FOUND, "EVIDENCE_NOT_FOUND", "The evidence was not found."));
    List<UUID> incidentIds =
        incidentLinkRepository
            .findAllByEvidenceIdAndOrganisationIdAndProjectIdOrderByLinkedAtAscIdAsc(
                evidenceId, organisationId, projectId)
            .stream()
            .map(IncidentEvidenceLink::getIncident)
            .map(incident -> incident.getId())
            .toList();
    List<UUID> eventIds =
        eventLinkRepository
            .findAllByEvidenceIdAndOrganisationIdAndProjectIdOrderByLinkedAtAscIdAsc(
                evidenceId, organisationId, projectId)
            .stream()
            .map(EvidenceEventLink::getEvent)
            .map(event -> event.getId())
            .toList();
    return new EvidenceView(evidence, incidentIds, eventIds);
  }

  private void requireProject(UUID organisationId, UUID projectId) {
    Project ignored =
        projectRepository
            .findByIdAndOrganisationId(projectId, organisationId)
            .orElseThrow(
                () ->
                    new TenantAccessException(
                        HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "The project was not found."));
  }

  public record EvidenceView(Evidence evidence, List<UUID> incidentIds, List<UUID> eventIds) {}
}
