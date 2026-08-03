package com.samharrison.incidentresponse.project;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRole;
import com.samharrison.incidentresponse.organisation.OrganisationRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

  private static final Set<OrganisationMembershipRole> WRITERS =
      Set.of(
          OrganisationMembershipRole.OWNER,
          OrganisationMembershipRole.ADMIN,
          OrganisationMembershipRole.MEMBER);

  private final ProjectRepository projectRepository;
  private final OrganisationRepository organisationRepository;
  private final TenantAccessService tenantAccessService;
  private final AuditRecorder auditRecorder;

  public ProjectService(
      ProjectRepository projectRepository,
      OrganisationRepository organisationRepository,
      TenantAccessService tenantAccessService,
      AuditRecorder auditRecorder) {
    this.projectRepository = projectRepository;
    this.organisationRepository = organisationRepository;
    this.tenantAccessService = tenantAccessService;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public Project create(
      UUID userId, UUID organisationId, String name, String slug, String description) {
    tenantAccessService.requireRole(organisationId, userId, WRITERS);

    if (projectRepository.existsByOrganisationIdAndSlug(organisationId, slug)) {
      throw new TenantAccessException(
          HttpStatus.CONFLICT,
          "PROJECT_SLUG_IN_USE",
          "The project slug is already in use in this organisation.");
    }

    Organisation organisation = requireOrganisation(organisationId);
    Project project =
        projectRepository.save(
            new Project(
                UUID.randomUUID(), organisation, name, slug, description, ProjectStatus.ACTIVE));

    auditRecorder.record(
        userId,
        organisationId,
        "PROJECT_CREATED",
        "PROJECT",
        project.getId(),
        "{\"slug\":\"" + project.getSlug() + "\"}");

    return project;
  }

  @Transactional(readOnly = true)
  public List<Project> list(UUID userId, UUID organisationId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return projectRepository.findAllByOrganisationIdOrderByNameAsc(organisationId);
  }

  @Transactional(readOnly = true)
  public Project get(UUID userId, UUID organisationId, UUID projectId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return requireProject(organisationId, projectId);
  }

  @Transactional
  public Project update(
      UUID userId, UUID organisationId, UUID projectId, String name, String description) {
    tenantAccessService.requireRole(organisationId, userId, WRITERS);
    Project project = requireProject(organisationId, projectId);
    project.updateDetails(name, description);

    auditRecorder.record(userId, organisationId, "PROJECT_UPDATED", "PROJECT", projectId, "{}");
    return project;
  }

  @Transactional
  public Project archive(UUID userId, UUID organisationId, UUID projectId) {
    tenantAccessService.requireRole(organisationId, userId, WRITERS);
    Project project = requireProject(organisationId, projectId);
    project.archive();

    auditRecorder.record(userId, organisationId, "PROJECT_ARCHIVED", "PROJECT", projectId, "{}");
    return project;
  }

  private Organisation requireOrganisation(UUID organisationId) {
    return organisationRepository
        .findById(organisationId)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND,
                    "ORGANISATION_NOT_FOUND",
                    "The organisation was not found."));
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
