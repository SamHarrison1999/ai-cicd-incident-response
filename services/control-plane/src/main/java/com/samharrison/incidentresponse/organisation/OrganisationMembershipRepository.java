package com.samharrison.incidentresponse.organisation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationMembershipRepository
    extends JpaRepository<OrganisationMembership, UUID> {

  Optional<OrganisationMembership> findByOrganisationIdAndUserId(UUID organisationId, UUID userId);

  boolean existsByOrganisationIdAndUserIdAndStatus(
      UUID organisationId, UUID userId, OrganisationMembershipStatus status);

  List<OrganisationMembership> findAllByUserIdAndStatus(
      UUID userId, OrganisationMembershipStatus status);
}
