package com.samharrison.incidentresponse.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.samharrison.incidentresponse.ingestion.EventProvider;
import com.samharrison.incidentresponse.ingestion.EventSource;
import com.samharrison.incidentresponse.ingestion.EventSourceRepository;
import com.samharrison.incidentresponse.ingestion.EventSourceStatus;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEventRepository;
import com.samharrison.incidentresponse.ingestion.NormalisedEventType;
import com.samharrison.incidentresponse.ingestion.PipelineRun;
import com.samharrison.incidentresponse.ingestion.PipelineRunRepository;
import com.samharrison.incidentresponse.ingestion.PipelineRunStatus;
import com.samharrison.incidentresponse.ingestion.SignatureAlgorithm;
import com.samharrison.incidentresponse.ingestion.WebhookDelivery;
import com.samharrison.incidentresponse.ingestion.WebhookDeliveryRepository;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.organisation.OrganisationRepository;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.time.Instant;
import java.util.List;
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
class EventIngestionPersistenceIntegrationTest {

  private static final Instant OCCURRED_AT = Instant.parse("2026-08-13T10:00:00Z");
  private static final Instant RECEIVED_AT = Instant.parse("2026-08-13T10:00:02Z");

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
  @Autowired private EventSourceRepository eventSourceRepository;
  @Autowired private WebhookDeliveryRepository webhookDeliveryRepository;
  @Autowired private PipelineRunRepository pipelineRunRepository;
  @Autowired private NormalisedCiEventRepository normalisedCiEventRepository;

  @BeforeEach
  void clearPersistenceModel() {
    normalisedCiEventRepository.deleteAll();
    pipelineRunRepository.deleteAll();
    webhookDeliveryRepository.deleteAll();
    eventSourceRepository.deleteAll();
    projectRepository.deleteAll();
    organisationRepository.deleteAll();
  }

  @Test
  void persistsCompleteProviderNeutralEventGraphInsideTenantBoundary() {
    TenantFixture fixture = createFixture("platform", "incident-response", "GitHub Actions");
    WebhookDelivery delivery = webhookDeliveryRepository.saveAndFlush(newDelivery(fixture, "d-1"));
    PipelineRun pipelineRun = pipelineRunRepository.saveAndFlush(newPipelineRun(fixture, "run-42"));
    NormalisedCiEvent event =
        normalisedCiEventRepository.saveAndFlush(
            newNormalisedEvent(fixture, delivery, pipelineRun));

    assertThat(
            eventSourceRepository.findByIdAndOrganisationId(
                fixture.eventSource().getId(), fixture.organisation().getId()))
        .contains(fixture.eventSource());
    assertThat(
            webhookDeliveryRepository.findByEventSourceIdAndProviderDeliveryId(
                fixture.eventSource().getId(), "d-1"))
        .contains(delivery);
    assertThat(
            pipelineRunRepository.findByEventSourceIdAndExternalRunIdAndAttempt(
                fixture.eventSource().getId(), "run-42", 1))
        .contains(pipelineRun);
    assertThat(normalisedCiEventRepository.findByWebhookDeliveryId(delivery.getId()))
        .contains(event);
    assertThat(event.getSourceFields())
        .containsExactly("workflow_run.id", "workflow_run.conclusion");
  }

  @Test
  void sameProviderDeliveryIdIsReusableAcrossSourcesButUniqueWithinSource() {
    TenantFixture first = createFixture("platform", "incident-response", "GitHub Actions");
    EventSource secondSource =
        eventSourceRepository.saveAndFlush(
            newEventSource(first.organisation(), first.project(), "Jenkins"));
    TenantFixture second = new TenantFixture(first.organisation(), first.project(), secondSource);

    webhookDeliveryRepository.saveAndFlush(newDelivery(first, "shared-delivery"));
    webhookDeliveryRepository.saveAndFlush(newDelivery(second, "shared-delivery"));

    assertThatThrownBy(
            () -> webhookDeliveryRepository.saveAndFlush(newDelivery(first, "shared-delivery")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void repositoryQueriesDoNotCrossOrganisationBoundary() {
    TenantFixture first = createFixture("first", "delivery-api", "GitHub Actions");
    TenantFixture second = createFixture("second", "delivery-api", "GitHub Actions");
    PipelineRun run = pipelineRunRepository.saveAndFlush(newPipelineRun(first, "run-1"));

    assertThat(
            pipelineRunRepository.findByIdAndOrganisationId(
                run.getId(), first.organisation().getId()))
        .contains(run);
    assertThat(
            pipelineRunRepository.findByIdAndOrganisationId(
                run.getId(), second.organisation().getId()))
        .isEmpty();
    assertThat(
            eventSourceRepository.findAllByProjectIdAndOrganisationIdOrderByDisplayNameAsc(
                first.project().getId(), second.organisation().getId()))
        .isEmpty();
  }

  @Test
  void oneDeliveryCannotProduceTwoNormalisedEvents() {
    TenantFixture fixture = createFixture("platform", "incident-response", "GitHub Actions");
    WebhookDelivery delivery = webhookDeliveryRepository.saveAndFlush(newDelivery(fixture, "d-1"));
    PipelineRun run = pipelineRunRepository.saveAndFlush(newPipelineRun(fixture, "run-42"));
    normalisedCiEventRepository.saveAndFlush(newNormalisedEvent(fixture, delivery, run));

    assertThatThrownBy(
            () ->
                normalisedCiEventRepository.saveAndFlush(
                    newNormalisedEvent(fixture, delivery, run)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  private TenantFixture createFixture(
      String organisationSlug, String projectSlug, String sourceName) {
    Organisation organisation =
        organisationRepository.saveAndFlush(
            new Organisation(UUID.randomUUID(), "Test Organisation", organisationSlug));
    Project project =
        projectRepository.saveAndFlush(
            new Project(
                UUID.randomUUID(),
                organisation,
                "Test Project",
                projectSlug,
                "Persistence integration test",
                ProjectStatus.ACTIVE));
    EventSource eventSource =
        eventSourceRepository.saveAndFlush(newEventSource(organisation, project, sourceName));
    return new TenantFixture(organisation, project, eventSource);
  }

  private EventSource newEventSource(
      Organisation organisation, Project project, String displayName) {
    return new EventSource(
        UUID.randomUUID(),
        organisation,
        project,
        displayName.equals("Jenkins") ? EventProvider.JENKINS : EventProvider.GITHUB_ACTIONS,
        displayName,
        EventSourceStatus.ENABLED,
        "local/event-sources/" + UUID.randomUUID(),
        SignatureAlgorithm.HMAC_SHA256,
        300,
        262_144);
  }

  private WebhookDelivery newDelivery(TenantFixture fixture, String deliveryId) {
    return new WebhookDelivery(
        UUID.randomUUID(),
        fixture.organisation(),
        fixture.project(),
        fixture.eventSource(),
        deliveryId,
        "workflow_run",
        "a".repeat(64),
        OCCURRED_AT,
        RECEIVED_AT);
  }

  private PipelineRun newPipelineRun(TenantFixture fixture, String externalRunId) {
    return new PipelineRun(
        UUID.randomUUID(),
        fixture.organisation(),
        fixture.project(),
        fixture.eventSource(),
        fixture.eventSource().getProvider(),
        externalRunId,
        "continuous-integration",
        1,
        PipelineRunStatus.FAILED,
        "a".repeat(40),
        "refs/heads/main",
        null,
        OCCURRED_AT);
  }

  private NormalisedCiEvent newNormalisedEvent(
      TenantFixture fixture, WebhookDelivery delivery, PipelineRun run) {
    return new NormalisedCiEvent(
        UUID.randomUUID(),
        fixture.organisation(),
        fixture.project(),
        fixture.eventSource(),
        delivery,
        run,
        "1.0",
        fixture.eventSource().getProvider(),
        NormalisedEventType.PIPELINE_RUN_COMPLETED,
        OCCURRED_AT,
        RECEIVED_AT,
        run.getExternalRunId(),
        run.getName(),
        run.getAttempt(),
        run.getStatus(),
        run.getCommitSha(),
        run.getGitRef(),
        run.getEnvironmentName(),
        "Workflow completed with a failed conclusion",
        List.of("workflow_run.id", "workflow_run.conclusion"));
  }

  private record TenantFixture(
      Organisation organisation, Project project, EventSource eventSource) {}
}
