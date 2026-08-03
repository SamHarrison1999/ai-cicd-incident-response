package com.samharrison.incidentresponse.organisation;

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
@RequestMapping("/api/v1/organisations")
public class OrganisationController {

  private final OrganisationService organisationService;
  private final CurrentUserProvider currentUserProvider;

  public OrganisationController(
      OrganisationService organisationService, CurrentUserProvider currentUserProvider) {
    this.organisationService = organisationService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  ResponseEntity<OrganisationResponse> create(
      Authentication authentication, @Valid @RequestBody CreateOrganisationRequest request) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    Organisation organisation = organisationService.create(userId, request.name(), request.slug());

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(organisation));
  }

  @GetMapping
  List<OrganisationResponse> list(Authentication authentication) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    return organisationService.listForUser(userId).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{organisationId}")
  OrganisationResponse get(Authentication authentication, @PathVariable UUID organisationId) {
    return toResponse(
        organisationService.get(currentUserProvider.requireUserId(authentication), organisationId));
  }

  @PatchMapping("/{organisationId}")
  OrganisationResponse update(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @Valid @RequestBody UpdateOrganisationRequest request) {
    return toResponse(
        organisationService.update(
            currentUserProvider.requireUserId(authentication), organisationId, request.name()));
  }

  private OrganisationResponse toResponse(Organisation organisation) {
    return new OrganisationResponse(
        organisation.getId(),
        organisation.getName(),
        organisation.getSlug(),
        organisation.getCreatedAt(),
        organisation.getUpdatedAt(),
        organisation.getVersion());
  }

  public record CreateOrganisationRequest(
      @NotBlank @Size(max = 120) String name,
      @NotBlank @Size(max = 80) @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug) {}

  public record UpdateOrganisationRequest(@NotBlank @Size(max = 120) String name) {}

  public record OrganisationResponse(
      UUID id,
      String name,
      String slug,
      java.time.Instant createdAt,
      java.time.Instant updatedAt,
      long version) {}
}
