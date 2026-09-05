package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.organisation.OrganisationRepository;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@Transactional
class EvidenceSearchPersistenceIntegrationTest {

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.1-alpine");

  @DynamicPropertySource
  static void configureDatabase(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @Autowired private OrganisationRepository organisationRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private EvidenceRepository evidenceRepository;

  @Test
  void searchSupportsAbsentTemporalAndCursorFilters() {
    Organisation organisation =
        organisationRepository.saveAndFlush(
            new Organisation(UUID.randomUUID(), "Evidence Search Organisation", "evidence-search"));

    Project project =
        projectRepository.saveAndFlush(
            new Project(
                UUID.randomUUID(),
                organisation,
                "Evidence Search Project",
                "evidence-search-project",
                "PostgreSQL evidence search regression test",
                ProjectStatus.ACTIVE));

    Evidence evidence =
        evidenceRepository.saveAndFlush(
            new Evidence(
                UUID.randomUUID(),
                organisation,
                project,
                EvidenceKind.LOG_EXCERPT,
                RetentionClass.STANDARD,
                "github-actions",
                "run-9001",
                Instant.parse("2026-09-05T10:00:00Z"),
                Instant.parse("2026-09-05T10:00:01Z"),
                "a".repeat(64),
                "Build failed during deployment.",
                1));

    var results =
        evidenceRepository.search(
            organisation.getId(),
            project.getId(),
            null,
            null,
            false,
            "",
            false,
            null,
            false,
            null,
            false,
            null,
            null,
            PageRequest.of(0, 25));

    assertThat(results.getContent()).containsExactly(evidence);
  }
}
