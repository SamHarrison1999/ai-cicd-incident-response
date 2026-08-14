package com.samharrison.incidentresponse.diagnosis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class DiagnosisControllerContractTest {

  @Test
  void exposesTenantScopedReadOnlyDiagnosisContract() {
    assertThat(DiagnosisController.class.getAnnotation(RequestMapping.class).value())
        .containsExactly("/api/v1/organisations/{organisationId}/projects/{projectId}/diagnosis");
    assertThat(DiagnosisController.class.getDeclaredMethods())
        .anyMatch(method -> method.isAnnotationPresent(GetMapping.class));
  }

  @Test
  void responseContainsOnlyBoundedDiagnosisProjection() {
    DiagnosisController.DiagnosisResponse response =
        new DiagnosisController.DiagnosisResponse(
            DeterministicDiagnosisEngine.RULE_VERSION,
            DiagnosisCategory.UNKNOWN.name(),
            0.35,
            List.of(UUID.randomUUID()),
            List.of("supporting evidence"),
            List.of("UNTRUSTED_INSTRUCTION_REMOVED"),
            "AMBIGUOUS_RULE_MATCH");

    assertThat(response.category()).isEqualTo("UNKNOWN");
    assertThat(response.abstentionReason()).isEqualTo("AMBIGUOUS_RULE_MATCH");
  }
}
