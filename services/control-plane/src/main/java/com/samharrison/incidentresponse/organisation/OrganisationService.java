package com.samharrison.incidentresponse.organisation;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserAccountRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganisationService {

  private static final Set<OrganisationMembershipRole> MANAGERS =
      Set.of(OrganisationMembershipRole.OWNER, OrganisationMembershipRole.ADMIN);

  private final OrganisationRepository organisationRepository;
  private final OrganisationMembershipRepository membershipRepository;
  private final UserAccountRepository userRepository;
  private final TenantAccessService tenantAccessService;
  private final AuditRecorder auditRecorder;

  public OrganisationService(
      OrganisationRepository organisationRepository,
      OrganisationMembershipRepository membershipRepository,
      UserAccountRepository userRepository,
      TenantAccessService tenantAccessService,
      AuditRecorder auditRecorder) {
    this.organisationRepository = organisationRepository;
    this.membershipRepository = membershipRepository;
    this.userRepository = userRepository;
    this.tenantAccessService = tenantAccessService;
    this.auditRecorder = auditRecorder;
  }

  @Transactional
  public Organisation create(UUID userId, String name, String slug) {
    if (organisationRepository.existsBySlug(slug)) {
      throw new TenantAccessException(
          HttpStatus.CONFLICT,
          "ORGANISATION_SLUG_IN_USE",
          "The organisation slug is already in use.");
    }

    UserAccount user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new TenantAccessException(
                        HttpStatus.UNAUTHORIZED,
                        "USER_NOT_FOUND",
                        "The authenticated user was not found."));

    Organisation organisation =
        organisationRepository.save(new Organisation(UUID.randomUUID(), name, slug));

    membershipRepository.save(
        new OrganisationMembership(
            UUID.randomUUID(),
            organisation,
            user,
            OrganisationMembershipRole.OWNER,
            OrganisationMembershipStatus.ACTIVE));

    auditRecorder.record(
        userId,
        organisation.getId(),
        "ORGANISATION_CREATED",
        "ORGANISATION",
        organisation.getId(),
        "{\"slug\":\"" + organisation.getSlug() + "\"}");

    return organisation;
  }

  @Transactional(readOnly = true)
  public List<Organisation> listForUser(UUID userId) {
    List<UUID> organisationIds =
        membershipRepository
            .findAllByUserIdAndStatus(userId, OrganisationMembershipStatus.ACTIVE)
            .stream()
            .map(membership -> membership.getOrganisation().getId())
            .toList();

    return organisationRepository.findAllByIdInOrderByNameAsc(organisationIds);
  }

  @Transactional(readOnly = true)
  public Organisation get(UUID userId, UUID organisationId) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return requireOrganisation(organisationId);
  }

  @Transactional
  public Organisation update(UUID userId, UUID organisationId, String name) {
    tenantAccessService.requireRole(organisationId, userId, MANAGERS);
    Organisation organisation = requireOrganisation(organisationId);
    organisation.rename(name);

    auditRecorder.record(
        userId, organisationId, "ORGANISATION_UPDATED", "ORGANISATION", organisationId, "{}");

    return organisation;
  }

  private Organisation requireOrganisation(UUID organisationId) {
    return organisationRepository
        .findById(organisationId)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND,
                    "ORGANISATION_NOT_FOUND",
                    "The organisation was not found."));
  }
}
