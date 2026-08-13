package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineRunService {
  private final PipelineRunRepository repository;
  private final TenantAccessService tenantAccessService;

  public PipelineRunService(
      PipelineRunRepository repository, TenantAccessService tenantAccessService) {
    this.repository = repository;
    this.tenantAccessService = tenantAccessService;
  }

  @Transactional(readOnly = true)
  public List<PipelineRun> list(UUID userId, UUID organisationId, UUID projectId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return repository.findAllByProjectIdAndOrganisationIdOrderByUpdatedAtDesc(
        projectId, organisationId);
  }

  @Transactional(readOnly = true)
  public PipelineRun get(UUID userId, UUID organisationId, UUID projectId, UUID pipelineRunId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return repository
        .findByIdAndOrganisationId(pipelineRunId, organisationId)
        .filter(run -> run.getProject().getId().equals(projectId))
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND,
                    "PIPELINE_RUN_NOT_FOUND",
                    "The pipeline run was not found."));
  }
}
