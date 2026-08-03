package com.samharrison.incidentresponse.organisation;

import com.samharrison.incidentresponse.identity.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "organisation_memberships",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_organisation_membership_organisation_user",
            columnNames = {"organisation_id", "user_id"}))
public class OrganisationMembership {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organisation_id", nullable = false)
  private Organisation organisation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserAccount user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private OrganisationMembershipRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private OrganisationMembershipStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected OrganisationMembership() {}

  public OrganisationMembership(
      UUID id,
      Organisation organisation,
      UserAccount user,
      OrganisationMembershipRole role,
      OrganisationMembershipStatus status) {
    this.id = Objects.requireNonNull(id);
    this.organisation = Objects.requireNonNull(organisation);
    this.user = Objects.requireNonNull(user);
    this.role = Objects.requireNonNull(role);
    this.status = Objects.requireNonNull(status);
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public Organisation getOrganisation() {
    return organisation;
  }

  public UserAccount getUser() {
    return user;
  }

  public OrganisationMembershipRole getRole() {
    return role;
  }

  public OrganisationMembershipStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public long getVersion() {
    return version;
  }
}
