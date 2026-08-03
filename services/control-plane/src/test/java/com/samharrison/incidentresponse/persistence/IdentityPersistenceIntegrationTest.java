package com.samharrison.incidentresponse.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.samharrison.incidentresponse.authentication.RefreshTokenSession;
import com.samharrison.incidentresponse.authentication.RefreshTokenSessionRepository;
import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserAccountRepository;
import com.samharrison.incidentresponse.identity.UserStatus;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.organisation.OrganisationMembership;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRepository;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRole;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipStatus;
import com.samharrison.incidentresponse.organisation.OrganisationRepository;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@Transactional
class IdentityPersistenceIntegrationTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.1-alpine");

  @DynamicPropertySource
  static void configureDatabase(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired private UserAccountRepository userRepository;
  @Autowired private OrganisationRepository organisationRepository;
  @Autowired private OrganisationMembershipRepository membershipRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private RefreshTokenSessionRepository refreshTokenRepository;

  @BeforeEach
  void clearPersistenceContext() {
    refreshTokenRepository.deleteAll();
    projectRepository.deleteAll();
    membershipRepository.deleteAll();
    organisationRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void storesAndFindsUserByNormalisedEmail() {
    UserAccount user = userRepository.saveAndFlush(newUser("sam@example.com"));

    assertThat(userRepository.findByNormalisedEmail("sam@example.com")).contains(user);
    assertThat(user.getCreatedAt()).isNotNull();
    assertThat(user.getUpdatedAt()).isNotNull();
  }

  @Test
  void rejectsDuplicateNormalisedEmail() {
    userRepository.saveAndFlush(newUser("sam@example.com"));

    assertThatThrownBy(() -> userRepository.saveAndFlush(newUser("sam@example.com")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void rejectsDuplicateMembershipForOrganisationAndUser() {
    UserAccount user = userRepository.saveAndFlush(newUser("member@example.com"));
    Organisation organisation =
        organisationRepository.saveAndFlush(newOrganisation("platform-team"));

    membershipRepository.saveAndFlush(
        new OrganisationMembership(
            UUID.randomUUID(),
            organisation,
            user,
            OrganisationMembershipRole.OWNER,
            OrganisationMembershipStatus.ACTIVE));

    assertThatThrownBy(
            () ->
                membershipRepository.saveAndFlush(
                    new OrganisationMembership(
                        UUID.randomUUID(),
                        organisation,
                        user,
                        OrganisationMembershipRole.MEMBER,
                        OrganisationMembershipStatus.ACTIVE)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void projectSlugIsUniqueWithinOrganisationButReusableAcrossOrganisations() {
    Organisation first = organisationRepository.saveAndFlush(newOrganisation("first-org"));
    Organisation second = organisationRepository.saveAndFlush(newOrganisation("second-org"));

    projectRepository.saveAndFlush(newProject(first, "incident-platform"));
    projectRepository.saveAndFlush(newProject(second, "incident-platform"));

    assertThatThrownBy(() -> projectRepository.saveAndFlush(newProject(first, "incident-platform")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findsProjectOnlyInsideRequestedOrganisationBoundary() {
    Organisation first = organisationRepository.saveAndFlush(newOrganisation("first-org"));
    Organisation second = organisationRepository.saveAndFlush(newOrganisation("second-org"));
    Project project = projectRepository.saveAndFlush(newProject(first, "delivery-api"));

    assertThat(projectRepository.findByIdAndOrganisationId(project.getId(), first.getId()))
        .contains(project);
    assertThat(projectRepository.findByIdAndOrganisationId(project.getId(), second.getId()))
        .isEmpty();
  }

  @Test
  void rejectsDuplicateRefreshTokenHash() {
    UserAccount user = userRepository.saveAndFlush(newUser("tokens@example.com"));
    Instant now = Instant.now();

    refreshTokenRepository.saveAndFlush(
        new RefreshTokenSession(
            UUID.randomUUID(),
            user,
            UUID.randomUUID(),
            "a".repeat(64),
            now.plus(7, ChronoUnit.DAYS),
            now));

    assertThatThrownBy(
            () ->
                refreshTokenRepository.saveAndFlush(
                    new RefreshTokenSession(
                        UUID.randomUUID(),
                        user,
                        UUID.randomUUID(),
                        "a".repeat(64),
                        now.plus(7, ChronoUnit.DAYS),
                        now)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  private UserAccount newUser(String email) {
    return new UserAccount(
        UUID.randomUUID(),
        email,
        email.toLowerCase(),
        "Test User",
        "{bcrypt}$2a$10$abcdefghijklmnopqrstuv",
        UserStatus.ACTIVE);
  }

  private Organisation newOrganisation(String slug) {
    return new Organisation(UUID.randomUUID(), "Test Organisation", slug);
  }

  private Project newProject(Organisation organisation, String slug) {
    return new Project(
        UUID.randomUUID(),
        organisation,
        "Test Project",
        slug,
        "Persistence integration test",
        ProjectStatus.ACTIVE);
  }
}
