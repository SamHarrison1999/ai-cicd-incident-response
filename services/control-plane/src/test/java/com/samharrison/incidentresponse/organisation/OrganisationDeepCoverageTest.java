package com.samharrison.incidentresponse.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserAccountRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessException;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class OrganisationDeepCoverageTest {
  private final UUID userId = UUID.randomUUID();
  private final UUID organisationId = UUID.randomUUID();

  @Test
  void createsListsGetsAndUpdatesOrganisation() {
    OrganisationRepository organisations = mock(OrganisationRepository.class);
    OrganisationMembershipRepository memberships = mock(OrganisationMembershipRepository.class);
    UserAccountRepository users = mock(UserAccountRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    AuditRecorder audit = mock(AuditRecorder.class);
    UserAccount user = mock(UserAccount.class);
    Organisation organisation = new Organisation(organisationId, "Old name", "old-name");
    when(organisations.existsBySlug("new-name")).thenReturn(false);
    when(users.findById(userId)).thenReturn(Optional.of(user));
    when(organisations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    OrganisationService service =
        new OrganisationService(organisations, memberships, users, tenant, audit);

    Organisation created = service.create(userId, "New name", "new-name");
    assertThat(created.getName()).isEqualTo("New name");

    OrganisationMembership membership = mock(OrganisationMembership.class);
    when(membership.getOrganisation()).thenReturn(organisation);
    when(memberships.findAllByUserIdAndStatus(userId, OrganisationMembershipStatus.ACTIVE))
        .thenReturn(List.of(membership));
    when(organisations.findAllByIdInOrderByNameAsc(List.of(organisationId)))
        .thenReturn(List.of(organisation));
    assertThat(service.listForUser(userId)).containsExactly(organisation);

    when(organisations.findById(organisationId)).thenReturn(Optional.of(organisation));
    assertThat(service.get(userId, organisationId)).isSameAs(organisation);
    when(tenant.requireRole(Mockito.eq(organisationId), Mockito.eq(userId), any()))
        .thenReturn(mock(OrganisationMembership.class));
    assertThat(service.update(userId, organisationId, "Renamed").getName()).isEqualTo("Renamed");
  }

  @Test
  void rejectsConflictsMissingUsersAndMissingOrganisations() {
    OrganisationRepository organisations = mock(OrganisationRepository.class);
    OrganisationMembershipRepository memberships = mock(OrganisationMembershipRepository.class);
    UserAccountRepository users = mock(UserAccountRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    AuditRecorder audit = mock(AuditRecorder.class);
    OrganisationService service =
        new OrganisationService(organisations, memberships, users, tenant, audit);
    when(organisations.existsBySlug("taken")).thenReturn(true);
    assertThatThrownBy(() -> service.create(userId, "Name", "taken"))
        .isInstanceOf(TenantAccessException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.CONFLICT);

    when(organisations.existsBySlug("missing-user")).thenReturn(false);
    when(users.findById(userId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.create(userId, "Name", "missing-user"))
        .isInstanceOf(TenantAccessException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.UNAUTHORIZED);

    when(organisations.findById(organisationId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.get(userId, organisationId))
        .isInstanceOf(TenantAccessException.class)
        .extracting("status")
        .isEqualTo(HttpStatus.NOT_FOUND);
    assertThatThrownBy(() -> service.update(userId, organisationId, "Name"))
        .isInstanceOf(TenantAccessException.class);
  }
}
