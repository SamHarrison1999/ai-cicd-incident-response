package com.samharrison.incidentresponse.evidence;

import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceSearchService {

  private final EvidenceRepository evidenceRepository;
  private final ProjectRepository projectRepository;
  private final TenantAccessService tenantAccessService;

  public EvidenceSearchService(
      EvidenceRepository evidenceRepository,
      ProjectRepository projectRepository,
      TenantAccessService tenantAccessService) {
    this.evidenceRepository = evidenceRepository;
    this.projectRepository = projectRepository;
    this.tenantAccessService = tenantAccessService;
  }

  @Transactional(readOnly = true)
  public SearchPage search(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      EvidenceSearchCriteria criteria,
      EvidenceCursor cursor) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    requireProject(organisationId, projectId);
    Slice<Evidence> results =
        evidenceRepository.search(
            organisationId,
            projectId,
            criteria.kind(),
            criteria.sourceSystem(),
            criteria.query(),
            criteria.occurredFrom(),
            criteria.occurredTo(),
            cursor == null ? null : cursor.occurredAt(),
            cursor == null ? null : cursor.id(),
            PageRequest.of(0, criteria.limit()));
    List<Evidence> content = results.getContent();
    EvidenceCursor nextCursor =
        results.hasNext() && !content.isEmpty()
            ? new EvidenceCursor(
                content.get(content.size() - 1).getOccurredAt(),
                content.get(content.size() - 1).getId())
            : null;
    return new SearchPage(content, nextCursor);
  }

  private void requireProject(UUID organisationId, UUID projectId) {
    projectRepository
        .findByIdAndOrganisationId(projectId, organisationId)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND", "The project was not found."));
  }

  public record SearchPage(List<Evidence> items, EvidenceCursor nextCursor) {}
}
