package com.samharrison.incidentresponse.diagnosis;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/diagnosis")
public class DiagnosisController {

  private final DiagnosisService service;
  private final CurrentUserProvider currentUserProvider;

  public DiagnosisController(DiagnosisService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  DiagnosisResponse diagnose(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId) {
    DiagnosisResult result =
        service.diagnose(
            currentUserProvider.requireUserId(authentication), organisationId, projectId);
    return new DiagnosisResponse(
        result.ruleVersion(),
        result.category().name(),
        result.confidence(),
        result.supportingSignalIds(),
        result.missingEvidence(),
        result.warnings(),
        result.abstentionReason());
  }

  public record DiagnosisResponse(
      String ruleVersion,
      String category,
      double confidence,
      List<UUID> supportingSignalIds,
      List<String> missingEvidence,
      List<String> warnings,
      String abstentionReason) {}
}
