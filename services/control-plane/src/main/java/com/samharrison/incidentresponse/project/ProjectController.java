package com.samharrison.incidentresponse.project;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final CurrentUserProvider currentUserProvider;

  public ProjectController(ProjectService projectService, CurrentUserProvider currentUserProvider) {
    this.projectService = projectService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  ResponseEntity<ProjectResponse> create(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @Valid @RequestBody CreateProjectRequest request) {
    Project project =
        projectService.create(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            request.name(),
            request.slug(),
            request.description());

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(project));
  }

  @GetMapping
  List<ProjectResponse> list(Authentication authentication, @PathVariable UUID organisationId) {
    return projectService
        .list(currentUserProvider.requireUserId(authentication), organisationId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{projectId}")
  ProjectResponse get(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId) {
    return toResponse(
        projectService.get(
            currentUserProvider.requireUserId(authentication), organisationId, projectId));
  }

  @PatchMapping("/{projectId}")
  ProjectResponse update(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @Valid @RequestBody UpdateProjectRequest request) {
    return toResponse(
        projectService.update(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            request.name(),
            request.description()));
  }

  @PostMapping("/{projectId}/archive")
  ProjectResponse archive(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId) {
    return toResponse(
        projectService.archive(
            currentUserProvider.requireUserId(authentication), organisationId, projectId));
  }

  private ProjectResponse toResponse(Project project) {
    return new ProjectResponse(
        project.getId(),
        project.getOrganisation().getId(),
        project.getName(),
        project.getSlug(),
        project.getDescription(),
        project.getStatus().name(),
        project.getCreatedAt(),
        project.getUpdatedAt(),
        project.getVersion());
  }

  public record CreateProjectRequest(
      @NotBlank @Size(max = 120) String name,
      @NotBlank @Size(max = 80) @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug,
      @Size(max = 500) String description) {}

  public record UpdateProjectRequest(
      @NotBlank @Size(max = 120) String name, @Size(max = 500) String description) {}

  public record ProjectResponse(
      UUID id,
      UUID organisationId,
      String name,
      String slug,
      String description,
      String status,
      java.time.Instant createdAt,
      java.time.Instant updatedAt,
      long version) {}
}
