package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.project.ProjectStatus;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventSourceServiceCoverageTest {

  @Test
  void listsAndCreatesTenantScopedEventSources() {
    EventSourceRepository sources = mock(EventSourceRepository.class);
    ProjectRepository projects = mock(ProjectRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    EventSourceService service = new EventSourceService(sources, projects, tenant);
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Project project = project(organisationId, projectId);
    EventSource saved = mock(EventSource.class);
    when(projects.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(project));
    when(sources.findAllByProjectIdAndOrganisationIdOrderByDisplayNameAsc(
            projectId, organisationId))
        .thenReturn(List.of(saved));
    when(sources.existsByProjectIdAndDisplayName(projectId, "GitHub")).thenReturn(false);
    when(sources.save(any())).thenReturn(saved);

    assertThat(service.list(UUID.randomUUID(), organisationId, projectId)).containsExactly(saved);
    assertThat(
            service.create(
                UUID.randomUUID(),
                organisationId,
                projectId,
                EventProvider.GITHUB_ACTIONS,
                "GitHub",
                EventSourceStatus.ENABLED,
                "secret-ref",
                SignatureAlgorithm.HMAC_SHA256,
                300,
                262144))
        .isSameAs(saved);
  }

  @Test
  void rejectsDuplicateDisplayNameAndMissingProject() {
    EventSourceRepository sources = mock(EventSourceRepository.class);
    ProjectRepository projects = mock(ProjectRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    EventSourceService service = new EventSourceService(sources, projects, tenant);
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    when(projects.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.of(project(organisationId, projectId)), Optional.empty());
    when(sources.existsByProjectIdAndDisplayName(projectId, "GitHub")).thenReturn(true);

    assertThatThrownBy(
            () ->
                service.create(
                    UUID.randomUUID(),
                    organisationId,
                    projectId,
                    EventProvider.GITHUB_ACTIONS,
                    "GitHub",
                    EventSourceStatus.ENABLED,
                    "secret-ref",
                    SignatureAlgorithm.HMAC_SHA256,
                    300,
                    262144))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The event source display name is already used by this project.");
    assertThatThrownBy(() -> service.list(UUID.randomUUID(), organisationId, projectId))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The project was not found.");
  }

  private static Project project(UUID organisationId, UUID projectId) {
    Organisation organisation = new Organisation(organisationId, "Platform", "platform");
    return new Project(
        projectId, organisation, "Project", "project", "Coverage project", ProjectStatus.ACTIVE);
  }
}
