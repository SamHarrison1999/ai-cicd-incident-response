package com.samharrison.incidentresponse.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserStatus;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdentityDomainModelTest {

  @Test
  void userRejectsBlankNormalisedEmail() {
    assertThatThrownBy(
            () ->
                new UserAccount(
                    UUID.randomUUID(), "sam@example.com", " ", "Sam", "hash", UserStatus.ACTIVE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("normalisedEmail must not be blank");
  }

  @Test
  void organisationRejectsBlankSlug() {
    assertThatThrownBy(() -> new Organisation(UUID.randomUUID(), "Platform", " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("slug must not be blank");
  }

  @Test
  void projectRejectsBlankName() {
    Organisation organisation = new Organisation(UUID.randomUUID(), "Platform", "platform");

    assertThatThrownBy(
            () ->
                new Project(
                    UUID.randomUUID(),
                    organisation,
                    " ",
                    "incident-response",
                    null,
                    ProjectStatus.ACTIVE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("name must not be blank");
  }
}
