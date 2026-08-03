package com.samharrison.incidentresponse.tenancy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserStatus;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.organisation.OrganisationMembership;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRepository;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRole;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipStatus;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantAccessServiceTest {

  @Mock private OrganisationMembershipRepository membershipRepository;

  @Test
  void hidesOrganisationWhenUserHasNoMembership() {
    UUID organisationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(membershipRepository.findByOrganisationIdAndUserId(organisationId, userId))
        .thenReturn(Optional.empty());

    TenantAccessService service = new TenantAccessService(membershipRepository);

    assertThatThrownBy(() -> service.requireActiveMembership(organisationId, userId))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The organisation was not found.");
  }

  @Test
  void rejectsViewerFromWriterOperation() {
    UUID organisationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Organisation organisation = new Organisation(organisationId, "Platform", "platform");
    UserAccount user =
        new UserAccount(
            userId,
            "viewer@example.com",
            "viewer@example.com",
            "Viewer",
            "hash",
            UserStatus.ACTIVE);
    OrganisationMembership membership =
        new OrganisationMembership(
            UUID.randomUUID(),
            organisation,
            user,
            OrganisationMembershipRole.VIEWER,
            OrganisationMembershipStatus.ACTIVE);

    when(membershipRepository.findByOrganisationIdAndUserId(organisationId, userId))
        .thenReturn(Optional.of(membership));

    TenantAccessService service = new TenantAccessService(membershipRepository);

    assertThatThrownBy(
            () ->
                service.requireRole(
                    organisationId, userId, Set.of(OrganisationMembershipRole.OWNER)))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("Your organisation role does not permit this operation.");
  }
}
