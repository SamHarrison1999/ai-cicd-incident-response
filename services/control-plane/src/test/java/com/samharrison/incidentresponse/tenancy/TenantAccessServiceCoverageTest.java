package com.samharrison.incidentresponse.tenancy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.organisation.OrganisationMembership;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRepository;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipStatus;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantAccessServiceCoverageTest {

  @Test
  void rejectsAnInactiveMembership() {
    OrganisationMembershipRepository repository = mock(OrganisationMembershipRepository.class);
    OrganisationMembership membership = mock(OrganisationMembership.class);
    UUID organisationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(repository.findByOrganisationIdAndUserId(organisationId, userId))
        .thenReturn(Optional.of(membership));
    when(membership.getStatus()).thenReturn(OrganisationMembershipStatus.SUSPENDED);

    assertThatThrownBy(
            () ->
                new TenantAccessService(repository).requireActiveMembership(organisationId, userId))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The organisation was not found.");
  }
}
