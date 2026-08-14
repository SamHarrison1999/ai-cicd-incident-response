package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRole;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

  private static final Set<OrganisationMembershipRole> INCIDENT_WRITERS =
      Set.of(
          OrganisationMembershipRole.OWNER,
          OrganisationMembershipRole.ADMIN,
          OrganisationMembershipRole.MEMBER);

  private final IncidentRepository repository;
  private final TenantAccessService tenantAccessService;
  private final AuditRecorder auditRecorder;

  public IncidentService(
      IncidentRepository repository,
      TenantAccessService tenantAccessService,
      AuditRecorder auditRecorder) {
    this.repository = repository;
    this.tenantAccessService = tenantAccessService;
    this.auditRecorder = auditRecorder;
  }

  @Transactional(readOnly = true)
  public List<Incident> list(UUID userId, UUID organisationId, UUID projectId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return repository.findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(
        organisationId, projectId);
  }

  @Transactional(readOnly = true)
  public Incident get(UUID userId, UUID organisationId, UUID projectId, UUID incidentId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return requireIncident(organisationId, projectId, incidentId);
  }

  @Transactional
  public Incident transition(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      UUID incidentId,
      IncidentStatus nextStatus,
      Instant occurredAt) {
    tenantAccessService.requireRole(organisationId, userId, INCIDENT_WRITERS);
    Incident incident = requireIncident(organisationId, projectId, incidentId);
    incident.transitionTo(nextStatus, occurredAt);
    auditRecorder.record(
        userId,
        organisationId,
        "INCIDENT_STATUS_CHANGED",
        "INCIDENT",
        incidentId,
        "{\"status\":\"" + nextStatus.name() + "\"}");
    return incident;
  }

  private Incident requireIncident(UUID organisationId, UUID projectId, UUID incidentId) {
    return repository
        .findByIdAndOrganisationIdAndProjectId(incidentId, organisationId, projectId)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND, "INCIDENT_NOT_FOUND", "The incident was not found."));
  }
}
