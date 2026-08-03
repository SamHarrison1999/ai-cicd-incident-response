package com.samharrison.incidentresponse.tenancy;

import com.samharrison.incidentresponse.organisation.OrganisationMembership;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRepository;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRole;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipStatus;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantAccessService {

  private final OrganisationMembershipRepository membershipRepository;

  public TenantAccessService(OrganisationMembershipRepository membershipRepository) {
    this.membershipRepository = membershipRepository;
  }

  @Transactional(readOnly = true)
  public OrganisationMembership requireActiveMembership(UUID organisationId, UUID userId) {
    return membershipRepository
        .findByOrganisationIdAndUserId(organisationId, userId)
        .filter(membership -> membership.getStatus() == OrganisationMembershipStatus.ACTIVE)
        .orElseThrow(
            () ->
                new TenantAccessException(
                    HttpStatus.NOT_FOUND,
                    "ORGANISATION_NOT_FOUND",
                    "The organisation was not found."));
  }

  @Transactional(readOnly = true)
  public OrganisationMembership requireRole(
      UUID organisationId, UUID userId, Set<OrganisationMembershipRole> allowedRoles) {
    OrganisationMembership membership = requireActiveMembership(organisationId, userId);

    if (!allowedRoles.contains(membership.getRole())) {
      throw new TenantAccessException(
          HttpStatus.FORBIDDEN,
          "INSUFFICIENT_ORGANISATION_ROLE",
          "Your organisation role does not permit this operation.");
    }

    return membership;
  }
}
