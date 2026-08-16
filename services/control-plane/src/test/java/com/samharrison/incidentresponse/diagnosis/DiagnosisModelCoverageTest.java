package com.samharrison.incidentresponse.diagnosis;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DiagnosisModelCoverageTest {

  @Test
  void rejectsInvalidSignals() {
    UUID id = UUID.randomUUID();
    assertThatThrownBy(() -> new DiagnosisSignal(id, id, " ", Instant.now(), "text"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DiagnosisSignal(id, id, "github", Instant.now(), " "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DiagnosisSignal(id, id, "github", Instant.now(), null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DiagnosisSignal(id, id, "github", Instant.now(), "x".repeat(4001)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DiagnosisSignal(id, id, null, Instant.now(), "text"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void enforcesDiagnosisResultAbstentionRulesAndConfidenceBounds() {
    assertThatThrownBy(
            () ->
                new DiagnosisResult(
                    "v1",
                    DiagnosisCategory.INSUFFICIENT_EVIDENCE,
                    0.0,
                    List.of(),
                    List.of(),
                    List.of(),
                    null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new DiagnosisResult(
                    "v1",
                    DiagnosisCategory.DEPENDENCY_FAILURE_SUSPECTED,
                    0.5,
                    List.of(),
                    List.of(),
                    List.of(),
                    "reason"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new DiagnosisResult(
                    "v1",
                    DiagnosisCategory.UNKNOWN,
                    1.1,
                    List.of(),
                    List.of(),
                    List.of(),
                    "reason"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new DiagnosisResult(
                    "v1",
                    DiagnosisCategory.DEPENDENCY_FAILURE_SUSPECTED,
                    Double.NaN,
                    List.of(),
                    List.of(),
                    List.of(),
                    null))
        .isInstanceOf(IllegalArgumentException.class);
    new DiagnosisResult(
        "v1", DiagnosisCategory.UNKNOWN, 0.5, List.of(), List.of(), List.of(), "unknown");
    assertThatThrownBy(
            () ->
                new DiagnosisResult(
                    "v1",
                    DiagnosisCategory.DEPENDENCY_FAILURE_SUSPECTED,
                    -0.1,
                    List.of(),
                    List.of(),
                    List.of(),
                    null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new DiagnosisResult(
                    "v1", DiagnosisCategory.UNKNOWN, 0.5, List.of(), List.of(), List.of(), " "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void coversNullSignalsWarningsAndAmbiguousRuleMatches() {
    DeterministicDiagnosisEngine engine = new DeterministicDiagnosisEngine();
    assertThatThrownBy(() -> engine.diagnose(null)).isInstanceOf(IllegalArgumentException.class);

    UUID evidenceId = UUID.randomUUID();
    DiagnosisResult warningResult =
        engine.diagnose(
            List.of(
                new DiagnosisSignal(
                    UUID.randomUUID(),
                    evidenceId,
                    "github",
                    Instant.now(),
                    "[UNTRUSTED_INSTRUCTION_REMOVED]")));
    org.assertj.core.api.Assertions.assertThat(warningResult.warnings())
        .contains("UNTRUSTED_INSTRUCTION_REMOVED");

    DiagnosisResult ambiguous =
        engine.diagnose(
            List.of(
                new DiagnosisSignal(
                    UUID.randomUUID(), evidenceId, "github", Instant.now(), "timeout"),
                new DiagnosisSignal(
                    UUID.randomUUID(),
                    evidenceId,
                    "github",
                    Instant.now().minusSeconds(1),
                    "deployment failed")));
    org.assertj.core.api.Assertions.assertThat(ambiguous.abstentionReason())
        .isEqualTo("AMBIGUOUS_RULE_MATCH");
  }
}
