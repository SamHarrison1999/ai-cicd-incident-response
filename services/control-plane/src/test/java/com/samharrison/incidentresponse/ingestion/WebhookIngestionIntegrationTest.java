package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.samharrison.incidentresponse.audit.AuditEventRepository;
import com.samharrison.incidentresponse.incident.IncidentCorrelationDecisionRecordRepository;
import com.samharrison.incidentresponse.incident.IncidentEventLinkRepository;
import com.samharrison.incidentresponse.incident.IncidentRepository;
import com.samharrison.incidentresponse.incident.IncidentStatus;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.organisation.OrganisationRepository;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WebhookIngestionIntegrationTest {

  private static final String SECRET_TEXT = "batch-3-integration-test-secret";
  private static final byte[] SECRET = SECRET_TEXT.getBytes(StandardCharsets.UTF_8);
  private static final byte[] PAYLOAD =
      "{\"workflow_run\":{\"id\":42,\"conclusion\":\"failure\"}}".getBytes(StandardCharsets.UTF_8);

  @Container
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18.1-alpine");

  @DynamicPropertySource
  static void configureApplication(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("application.webhooks.secrets.integration-test", () -> SECRET_TEXT);
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private WebhookSignatureService signatureService;
  @Autowired private AuditEventRepository auditEventRepository;
  @Autowired private IncidentCorrelationDecisionRecordRepository correlationDecisionRepository;
  @Autowired private IncidentEventLinkRepository incidentEventLinkRepository;
  @Autowired private IncidentRepository incidentRepository;
  @Autowired private OrganisationRepository organisationRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private EventSourceRepository eventSourceRepository;
  @Autowired private WebhookDeliveryRepository webhookDeliveryRepository;
  @Autowired private NormalisedCiEventRepository normalisedCiEventRepository;
  @Autowired private PipelineRunRepository pipelineRunRepository;

  @BeforeEach
  void clearPersistenceModel() {
    auditEventRepository.deleteAll();
    correlationDecisionRepository.deleteAll();
    incidentEventLinkRepository.deleteAll();
    incidentRepository.deleteAll();
    normalisedCiEventRepository.deleteAll();
    pipelineRunRepository.deleteAll();
    webhookDeliveryRepository.deleteAll();
    eventSourceRepository.deleteAll();
    projectRepository.deleteAll();
    organisationRepository.deleteAll();
  }

  @Test
  void acceptsValidSignedWebhookWithoutBearerAuthentication() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String timestamp = currentTimestamp();

    perform(source, "delivery-1", "workflow_run", timestamp, PAYLOAD)
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.duplicate").value(false))
        .andExpect(jsonPath("$.status").value("RECEIVED"));

    assertThat(webhookDeliveryRepository.count()).isEqualTo(1);
    assertThat(normalisedCiEventRepository.count()).isEqualTo(1);
    assertThat(pipelineRunRepository.count()).isEqualTo(1);
    assertThat(incidentRepository.count()).isEqualTo(1);
    assertThat(incidentEventLinkRepository.count()).isEqualTo(1);
    assertThat(correlationDecisionRepository.count()).isEqualTo(1);

    WebhookDelivery delivery = webhookDeliveryRepository.findAll().getFirst();
    assertThat(delivery.getPayloadSha256()).isEqualTo(signatureService.sha256Hex(PAYLOAD));

    NormalisedCiEvent event = normalisedCiEventRepository.findAll().getFirst();
    var incident = incidentRepository.findAll().getFirst();
    var decision = correlationDecisionRepository.findByEventId(event.getId()).orElseThrow();

    assertThat(incident.getStatus()).isEqualTo(IncidentStatus.DETECTED);
    assertThat(decision.getIncidentId()).isEqualTo(incident.getId());
    assertThat(decision.getEventId()).isEqualTo(event.getId());
    assertThat(incidentEventLinkRepository.findByEventId(event.getId()))
        .hasValueSatisfying(
            link -> assertThat(link.getIncident().getId()).isEqualTo(incident.getId()));
  }

  @Test
  void timelineQuerySupportsAbsentTemporalAndCursorFilters() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String timestamp = currentTimestamp();

    perform(source, "timeline-delivery", "workflow_run", timestamp, PAYLOAD)
        .andExpect(status().isAccepted());

    var page =
        normalisedCiEventRepository.searchTimeline(
            source.getProject().getId(),
            source.getOrganisation().getId(),
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            false,
            null,
            false,
            null,
            null,
            null,
            PageRequest.of(0, 25));

    assertThat(page.getContent()).hasSize(1);
  }

  @Test
  void returnsExistingAcceptanceForIdempotentRetry() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String timestamp = currentTimestamp();

    perform(source, "delivery-1", "workflow_run", timestamp, PAYLOAD)
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.duplicate").value(false));
    perform(source, "delivery-1", "workflow_run", timestamp, PAYLOAD)
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.duplicate").value(true));

    assertThat(webhookDeliveryRepository.count()).isEqualTo(1);
    assertThat(normalisedCiEventRepository.count()).isEqualTo(1);
    assertThat(pipelineRunRepository.count()).isEqualTo(1);
    assertThat(incidentRepository.count()).isEqualTo(1);
    assertThat(incidentEventLinkRepository.count()).isEqualTo(1);
    assertThat(correlationDecisionRepository.count()).isEqualTo(1);
  }

  @Test
  void rejectsDeliveryIdentifierReuseWithDifferentPayload() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String timestamp = currentTimestamp();
    byte[] differentPayload = "{\"workflow_run\":{\"id\":43}}".getBytes(StandardCharsets.UTF_8);

    perform(source, "delivery-1", "workflow_run", timestamp, PAYLOAD)
        .andExpect(status().isAccepted());
    perform(source, "delivery-1", "workflow_run", timestamp, differentPayload)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("WEBHOOK_DELIVERY_PAYLOAD_CONFLICT"));

    assertThat(webhookDeliveryRepository.count()).isEqualTo(1);
  }

  @Test
  void rejectsDeliveryIdentifierReuseWithDifferentEventType() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String timestamp = currentTimestamp();

    perform(source, "delivery-1", "workflow_run", timestamp, PAYLOAD)
        .andExpect(status().isAccepted());
    perform(source, "delivery-1", "check_suite", timestamp, PAYLOAD)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("WEBHOOK_DELIVERY_PAYLOAD_CONFLICT"));

    assertThat(webhookDeliveryRepository.count()).isEqualTo(1);
  }

  @Test
  void rejectsInvalidSignatureWithoutPersistingDelivery() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String timestamp = currentTimestamp();

    mockMvc
        .perform(
            post(path(source))
                .contentType(MediaType.APPLICATION_JSON)
                .header(WebhookHeaders.DELIVERY_ID, "delivery-1")
                .header(WebhookHeaders.EVENT_TYPE, "workflow_run")
                .header(WebhookHeaders.DELIVERY_TIMESTAMP, timestamp)
                .header(WebhookHeaders.SIGNATURE, "sha256=" + "0".repeat(64))
                .content(PAYLOAD))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("WEBHOOK_SIGNATURE_INVALID"));

    assertThat(webhookDeliveryRepository.count()).isZero();
  }

  @Test
  void rejectsMetadataTamperingBecauseHeadersAreSigned() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String timestamp = currentTimestamp();
    String signature =
        signatureService.calculateSignature(
            "delivery-1", "workflow_run", timestamp, PAYLOAD, SECRET);

    mockMvc
        .perform(
            post(path(source))
                .contentType(MediaType.APPLICATION_JSON)
                .header(WebhookHeaders.DELIVERY_ID, "delivery-2")
                .header(WebhookHeaders.EVENT_TYPE, "workflow_run")
                .header(WebhookHeaders.DELIVERY_TIMESTAMP, timestamp)
                .header(WebhookHeaders.SIGNATURE, signature)
                .content(PAYLOAD))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("WEBHOOK_SIGNATURE_INVALID"));

    assertThat(webhookDeliveryRepository.count()).isZero();
  }

  @Test
  void rejectsSignedRequestOutsideTimestampTolerance() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String staleTimestamp = Instant.now().minus(10, ChronoUnit.MINUTES).toString();

    perform(source, "delivery-1", "workflow_run", staleTimestamp, PAYLOAD)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("WEBHOOK_TIMESTAMP_OUTSIDE_TOLERANCE"));

    assertThat(webhookDeliveryRepository.count()).isZero();
  }

  @Test
  void rejectsOversizedPayloadBeforePersistence() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 8);
    String timestamp = currentTimestamp();

    perform(source, "delivery-1", "workflow_run", timestamp, PAYLOAD)
        .andExpect(status().is(413))
        .andExpect(jsonPath("$.code").value("WEBHOOK_PAYLOAD_TOO_LARGE"));

    assertThat(webhookDeliveryRepository.count()).isZero();
  }

  @Test
  void rejectsMalformedJsonAfterSuccessfulSignatureVerification() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.ENABLED, 262_144);
    String timestamp = currentTimestamp();
    byte[] invalidJson = "not-json".getBytes(StandardCharsets.UTF_8);

    perform(source, "delivery-1", "workflow_run", timestamp, invalidJson)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("WEBHOOK_JSON_INVALID"));

    assertThat(webhookDeliveryRepository.count()).isZero();
  }

  @Test
  void hidesDisabledEventSource() throws Exception {
    EventSource source = createEventSource(EventSourceStatus.DISABLED, 262_144);
    String timestamp = currentTimestamp();

    perform(source, "delivery-1", "workflow_run", timestamp, PAYLOAD)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("EVENT_SOURCE_NOT_FOUND"));

    assertThat(webhookDeliveryRepository.count()).isZero();
  }

  private ResultActions perform(
      EventSource source, String deliveryId, String eventType, String timestamp, byte[] payload)
      throws Exception {
    String signature =
        signatureService.calculateSignature(deliveryId, eventType, timestamp, payload, SECRET);
    return mockMvc.perform(
        post(path(source))
            .contentType(MediaType.APPLICATION_JSON)
            .header(WebhookHeaders.DELIVERY_ID, deliveryId)
            .header(WebhookHeaders.EVENT_TYPE, eventType)
            .header(WebhookHeaders.DELIVERY_TIMESTAMP, timestamp)
            .header(WebhookHeaders.SIGNATURE, signature)
            .content(payload));
  }

  private EventSource createEventSource(EventSourceStatus status, int maximumPayloadSize) {
    Organisation organisation =
        organisationRepository.saveAndFlush(
            new Organisation(UUID.randomUUID(), "Test Organisation", "test-organisation"));
    Project project =
        projectRepository.saveAndFlush(
            new Project(
                UUID.randomUUID(),
                organisation,
                "Test Project",
                "test-project",
                "Webhook integration test",
                ProjectStatus.ACTIVE));
    return eventSourceRepository.saveAndFlush(
        new EventSource(
            UUID.randomUUID(),
            organisation,
            project,
            EventProvider.GITHUB_ACTIONS,
            "GitHub Actions",
            status,
            "integration-test",
            SignatureAlgorithm.HMAC_SHA256,
            300,
            maximumPayloadSize));
  }

  private String path(EventSource source) {
    return "/api/v1/event-sources/" + source.getId() + "/deliveries";
  }

  private String currentTimestamp() {
    return Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
  }
}
