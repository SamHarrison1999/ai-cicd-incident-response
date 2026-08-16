package com.samharrison.incidentresponse.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.organisation.OrganisationRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceCoverageTest {

  @Mock private ProjectRepository projectRepository;
  @Mock private OrganisationRepository organisationRepository;
  @Mock private TenantAccessService tenantAccessService;
  @Mock private AuditRecorder auditRecorder;

  private ProjectService service;
  private UUID userId;
  private UUID organisationId;
  private UUID projectId;
  private Organisation organisation;
  private Project project;

  @BeforeEach
  void setUp() {
    service =
        new ProjectService(
            projectRepository, organisationRepository, tenantAccessService, auditRecorder);
    userId = UUID.randomUUID();
    organisationId = UUID.randomUUID();
    projectId = UUID.randomUUID();
    organisation = new Organisation(organisationId, "Example", "example");
    project =
        new Project(projectId, organisation, "API", "api", "Description", ProjectStatus.ACTIVE);
  }

  @Test
  void createPersistsProjectAndRecordsAudit() {
    when(projectRepository.existsByOrganisationIdAndSlug(organisationId, "api")).thenReturn(false);
    when(organisationRepository.findById(organisationId)).thenReturn(Optional.of(organisation));
    when(projectRepository.save(any(Project.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Project result = service.create(userId, organisationId, "API", "api", "Description");

    assertThat(result.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    verify(auditRecorder)
        .record(
            userId,
            organisationId,
            "PROJECT_CREATED",
            "PROJECT",
            result.getId(),
            "{\"slug\":\"api\"}");
  }

  @Test
  void createRejectsMissingOrganisation() {
    when(projectRepository.existsByOrganisationIdAndSlug(organisationId, "api")).thenReturn(false);
    when(organisationRepository.findById(organisationId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(userId, organisationId, "API", "api", null))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The organisation was not found.");
  }

  @Test
  void listAndGetUseTenantScopedQueries() {
    when(projectRepository.findAllByOrganisationIdOrderByNameAsc(organisationId))
        .thenReturn(List.of(project));
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(project));

    assertThat(service.list(userId, organisationId)).containsExactly(project);
    assertThat(service.get(userId, organisationId, projectId)).isSameAs(project);
    verify(tenantAccessService, org.mockito.Mockito.times(2))
        .requireActiveMembership(organisationId, userId);
  }

  @Test
  void updateChangesDetailsAndRecordsAudit() {
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(project));

    Project result =
        service.update(userId, organisationId, projectId, "Updated API", "New details");

    assertThat(result.getName()).isEqualTo("Updated API");
    assertThat(result.getDescription()).isEqualTo("New details");
    verify(auditRecorder)
        .record(userId, organisationId, "PROJECT_UPDATED", "PROJECT", projectId, "{}");
  }

  @Test
  void archiveChangesStatusAndRecordsAudit() {
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(project));

    Project result = service.archive(userId, organisationId, projectId);

    assertThat(result.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
    verify(auditRecorder)
        .record(userId, organisationId, "PROJECT_ARCHIVED", "PROJECT", projectId, "{}");
  }

  @Test
  void updateAndArchiveRejectMissingProject() {
    when(projectRepository.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.update(userId, organisationId, projectId, "Updated", "details"))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The project was not found.");
    assertThatThrownBy(() -> service.archive(userId, organisationId, projectId))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The project was not found.");
  }
}
