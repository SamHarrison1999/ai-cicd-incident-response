package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.organisation.OrganisationMembershipRole;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventSourceService {
  private static final Set<OrganisationMembershipRole> WRITERS =
      Set.of(
          OrganisationMembershipRole.OWNER,
          OrganisationMembershipRole.ADMIN,
          OrganisationMembershipRole.MEMBER);

  private final EventSourceRepository eventSourceRepository;
  private final ProjectRepository projectRepository;
  private final TenantAccessService tenantAccessService;

  public EventSourceService(
      EventSourceRepository eventSourceRepository,
      ProjectRepository projectRepository,
      TenantAccessService tenantAccessService) {
    this.eventSourceRepository = eventSourceRepository;
    this.projectRepository = projectRepository;
    this.tenantAccessService = tenantAccessService;
  }

  @Transactional(readOnly = true)
  public List<EventSource> list(UUID userId, UUID organisationId, UUID projectId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    requireProject(organisationId, projectId);
    return eventSourceRepository.findAllByProjectIdAndOrganisationIdOrderByDisplayNameAsc(
        projectId, organisationId);
  }

  @Transactional
  public EventSource create(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      EventProvider provider,
      String displayName,
      EventSourceStatus status,
      String signingSecretReference,
      SignatureAlgorithm signatureAlgorithm,
      int timestampToleranceSeconds,
      int maxPayloadSizeBytes) {
    tenantAccessService.requireRole(organisationId, userId, WRITERS);
    Project project = requireProject(organisationId, projectId);
    if (eventSourceRepository.existsByProjectIdAndDisplayName(projectId, displayName)) {
      throw new TenantAccessException(
          HttpStatus.CONFLICT,
          "EVENT_SOURCE_DISPLAY_NAME_IN_USE",
          "The event source display name is already used by this project.");
    }
    return eventSourceRepository.save(
        new EventSource(
            UUID.randomUUID(),
            project.getOrganisation(),
            project,
            provider,
            displayName,
            status,
            signingSecretReference,
            signatureAlgorithm,
            timestampToleranceSeconds,
            maxPayloadSizeBytes));
  }

  private Project requireProject(UUID organisationId, UUID projectId) {
    return projectRepository
        .findByIdAndOrganisationId(projectId, organisationId)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "The project was not found."));
  }
}
