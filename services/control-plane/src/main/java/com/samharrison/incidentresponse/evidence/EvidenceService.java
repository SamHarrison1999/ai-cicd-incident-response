package com.samharrison.incidentresponse.evidence;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceService {

  private final EvidenceRepository evidenceRepository;
  private final ProjectRepository projectRepository;
  private final TenantAccessService tenantAccessService;
  private final AuditRecorder auditRecorder;

  public EvidenceService(
      EvidenceRepository evidenceRepository,
      ProjectRepository projectRepository,
      TenantAccessService tenantAccessService,
      AuditRecorder auditRecorder) {
    this.evidenceRepository = evidenceRepository;
    this.projectRepository = projectRepository;
    this.tenantAccessService = tenantAccessService;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public Evidence create(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      EvidenceKind kind,
      RetentionClass retentionClass,
      String sourceSystem,
      String sourceReference,
      Instant occurredAt,
      String rawContent) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    Project project = requireProject(organisationId, projectId);
    SanitisedEvidence sanitised = EvidenceSanitiser.sanitise(rawContent);
    String hash = EvidenceContentHasher.sha256Hex(sanitised.content());
    Evidence evidence =
        new Evidence(
            UUID.randomUUID(),
            project.getOrganisation(),
            project,
            kind,
            retentionClass,
            sourceSystem,
            sourceReference,
            occurredAt,
            Instant.now(),
            hash,
            sanitised.content(),
            sanitised.lineCount());
    Evidence saved = evidenceRepository.save(evidence);
    auditRecorder.record(
        userId,
        organisationId,
        "EVIDENCE_CREATED",
        "EVIDENCE",
        saved.getId(),
        "{\"kind\":\"" + kind.name() + "\",\"retentionClass\":\"" + retentionClass.name() + "\"}");
    return saved;
  }

  @Transactional(readOnly = true)
  public List<Evidence> list(UUID userId, UUID organisationId, UUID projectId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    requireProject(organisationId, projectId);
    return evidenceRepository.findAllByOrganisationIdAndProjectIdOrderByOccurredAtDescIdDesc(
        organisationId, projectId);
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
