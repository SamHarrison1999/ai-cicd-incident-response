package com.samharrison.incidentresponse.diagnosis;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.evidence.Evidence;
import com.samharrison.incidentresponse.evidence.EvidenceRepository;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiagnosisService {

  private final EvidenceRepository evidenceRepository;
  private final ProjectRepository projectRepository;
  private final TenantAccessService tenantAccessService;
  private final AuditRecorder auditRecorder;
  private final DeterministicDiagnosisEngine engine;

  public DiagnosisService(
      EvidenceRepository evidenceRepository,
      ProjectRepository projectRepository,
      TenantAccessService tenantAccessService,
      AuditRecorder auditRecorder,
      DeterministicDiagnosisEngine engine) {
    this.evidenceRepository = evidenceRepository;
    this.projectRepository = projectRepository;
    this.tenantAccessService = tenantAccessService;
    this.auditRecorder = auditRecorder;
    this.engine = engine;
  }

  @Transactional(readOnly = true)
  public DiagnosisResult diagnose(UUID userId, UUID organisationId, UUID projectId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    requireProject(organisationId, projectId);
    List<DiagnosisSignal> signals =
        evidenceRepository
            .findAllByOrganisationIdAndProjectIdOrderByOccurredAtDescIdDesc(
                organisationId, projectId)
            .stream()
            .limit(DeterministicDiagnosisEngine.MAX_SIGNALS)
            .map(DiagnosisService::toSignal)
            .toList();
    DiagnosisResult result = engine.diagnose(signals);
    auditRecorder.record(
        userId,
        organisationId,
        "DIAGNOSIS_VIEWED",
        "PROJECT",
        projectId,
        "{\"ruleVersion\":\""
            + result.ruleVersion()
            + "\",\"category\":\""
            + result.category()
            + "\"}");
    return result;
  }

  private static DiagnosisSignal toSignal(Evidence evidence) {
    return new DiagnosisSignal(
        evidence.getId(),
        evidence.getId(),
        evidence.getSourceSystem(),
        evidence.getOccurredAt(),
        evidence.getContent());
  }

  private void requireProject(UUID organisationId, UUID projectId) {
    projectRepository
        .findByIdAndOrganisationId(projectId, organisationId)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "The project was not found."));
  }
}
