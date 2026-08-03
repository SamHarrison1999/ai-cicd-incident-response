package com.samharrison.incidentresponse.project;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.organisation.OrganisationRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock private ProjectRepository projectRepository;
  @Mock private OrganisationRepository organisationRepository;
  @Mock private TenantAccessService tenantAccessService;
  @Mock private AuditRecorder auditRecorder;

  @Test
  void crossTenantProjectIdentifierIsNotReturned() {
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.empty());

    ProjectService service =
        new ProjectService(
            projectRepository, organisationRepository, tenantAccessService, auditRecorder);

    assertThatThrownBy(() -> service.get(userId, organisationId, projectId))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The project was not found.");

    verify(tenantAccessService).requireActiveMembership(organisationId, userId);
  }

  @Test
  void duplicateSlugIsRejectedWithinOrganisation() {
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    when(projectRepository.existsByOrganisationIdAndSlug(organisationId, "api")).thenReturn(true);

    ProjectService service =
        new ProjectService(
            projectRepository, organisationRepository, tenantAccessService, auditRecorder);

    assertThatThrownBy(() -> service.create(userId, organisationId, "API", "api", null))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The project slug is already in use in this organisation.");
  }
}
