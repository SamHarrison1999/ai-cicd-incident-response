package com.samharrison.incidentresponse.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.samharrison.incidentresponse.organisation.OrganisationMembershipRole;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DemoCiSimulationService {

  private static final String DEMO_SOURCE_NAME = "Browser Demo GitHub Actions";
  private static final String DEMO_SECRET_REFERENCE = "local-simulator";
  private static final String DEMO_COMMIT_SHA = "0123456789abcdef0123456789abcdef01234567";

  private static final Set<OrganisationMembershipRole> WRITERS =
      Set.of(
          OrganisationMembershipRole.OWNER,
          OrganisationMembershipRole.ADMIN,
          OrganisationMembershipRole.MEMBER);

  private final EventSourceRepository eventSourceRepository;
  private final EventSourceService eventSourceService;
  private final TenantAccessService tenantAccessService;
  private final WebhookSecretResolver secretResolver;
  private final WebhookSignatureService signatureService;
  private final WebhookIngestionService ingestionService;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Clock clock;

  public DemoCiSimulationService(
      EventSourceRepository eventSourceRepository,
      EventSourceService eventSourceService,
      TenantAccessService tenantAccessService,
      WebhookSecretResolver secretResolver,
      WebhookSignatureService signatureService,
      WebhookIngestionService ingestionService,
      Clock clock) {
    this.eventSourceRepository = eventSourceRepository;
    this.eventSourceService = eventSourceService;
    this.tenantAccessService = tenantAccessService;
    this.secretResolver = secretResolver;
    this.signatureService = signatureService;
    this.ingestionService = ingestionService;
    this.clock = clock;
  }

  public DemoCiSimulationResult simulate(
      UUID userId,
      UUID organisationId,
      UUID projectId,
      String pipelineName,
      String branch,
      DemoCiOutcome outcome) {
    tenantAccessService.requireRole(organisationId, userId, WRITERS);

    EventSource eventSource =
        eventSourceRepository
            .findByProjectIdAndOrganisationIdAndDisplayName(
                projectId, organisationId, DEMO_SOURCE_NAME)
            .orElseGet(
                () ->
                    eventSourceService.create(
                        userId,
                        organisationId,
                        projectId,
                        EventProvider.GITHUB_ACTIONS,
                        DEMO_SOURCE_NAME,
                        EventSourceStatus.ENABLED,
                        DEMO_SECRET_REFERENCE,
                        SignatureAlgorithm.HMAC_SHA256,
                        300,
                        262144));

    Instant now = clock.instant();
    String timestamp = now.toString();
    String providerDeliveryId = UUID.randomUUID().toString();
    String externalRunId = Long.toString(now.toEpochMilli());

    ObjectNode payloadRoot = objectMapper.createObjectNode();
    payloadRoot.put("action", "completed");

    ObjectNode workflowRun = payloadRoot.putObject("workflow_run");
    workflowRun.put("id", now.toEpochMilli());
    workflowRun.put("name", pipelineName);
    workflowRun.put("run_attempt", 1);
    workflowRun.put("status", "completed");
    workflowRun.put("conclusion", outcome == DemoCiOutcome.FAILED ? "failure" : "success");
    workflowRun.put("head_sha", DEMO_COMMIT_SHA);
    workflowRun.put("head_branch", branch);
    workflowRun.put("updated_at", timestamp);

    byte[] payload = payloadRoot.toString().getBytes(StandardCharsets.UTF_8);

    byte[] secret =
        secretResolver
            .resolve(eventSource.getSigningSecretReference())
            .orElseThrow(WebhookIngestionException::secretUnavailable);

    String signature;

    try {
      signature =
          signatureService.calculateSignature(
              providerDeliveryId, "workflow_run", timestamp, payload, secret);
    } finally {
      Arrays.fill(secret, (byte) 0);
    }

    WebhookAcceptanceResponse acceptance =
        ingestionService.ingest(
            eventSource.getId(),
            providerDeliveryId,
            "workflow_run",
            timestamp,
            signature,
            "application/json",
            payload.length,
            new ByteArrayInputStream(payload));

    return new DemoCiSimulationResult(
        eventSource.getId(),
        providerDeliveryId,
        externalRunId,
        pipelineName,
        branch,
        outcome,
        acceptance);
  }

  public record DemoCiSimulationResult(
      UUID eventSourceId,
      String providerDeliveryId,
      String externalRunId,
      String pipelineName,
      String branch,
      DemoCiOutcome outcome,
      WebhookAcceptanceResponse acceptance) {}
}
