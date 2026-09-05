package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/demo/ci-runs")
public class DemoCiSimulationController {

  private final DemoCiSimulationService simulationService;
  private final CurrentUserProvider currentUserProvider;

  public DemoCiSimulationController(
      DemoCiSimulationService simulationService, CurrentUserProvider currentUserProvider) {
    this.simulationService = simulationService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping
  ResponseEntity<DemoCiSimulationResponse> simulate(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @Valid @RequestBody DemoCiSimulationRequest request) {
    DemoCiSimulationService.DemoCiSimulationResult result =
        simulationService.simulate(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            request.pipelineName(),
            request.branch(),
            request.outcome());

    WebhookAcceptanceResponse acceptance = result.acceptance();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new DemoCiSimulationResponse(
                result.eventSourceId(),
                result.providerDeliveryId(),
                result.externalRunId(),
                result.pipelineName(),
                result.branch(),
                result.outcome(),
                acceptance.deliveryId(),
                acceptance.duplicate(),
                acceptance.status().name(),
                acceptance.receivedAt()));
  }

  public record DemoCiSimulationRequest(
      @NotBlank @Size(max = 120) String pipelineName,
      @NotBlank @Size(max = 255) String branch,
      @NotNull DemoCiOutcome outcome) {}

  public record DemoCiSimulationResponse(
      UUID eventSourceId,
      String providerDeliveryId,
      String externalRunId,
      String pipelineName,
      String branch,
      DemoCiOutcome outcome,
      UUID deliveryId,
      boolean duplicate,
      String deliveryStatus,
      Instant receivedAt) {}
}
