package com.samharrison.incidentresponse.diagnosis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DiagnosisEngineTest {

  private final DeterministicDiagnosisEngine engine = new DeterministicDiagnosisEngine();

  @Test
  void identifiesDependencyFailureWithBoundedConfidence() {
    UUID signalId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    DiagnosisResult result =
        engine.diagnose(
            List.of(
                new DiagnosisSignal(
                    signalId,
                    UUID.randomUUID(),
                    "github",
                    Instant.parse("2026-08-14T10:00:00Z"),
                    "deployment observed timeout from upstream dependency")));

    assertThat(result.category()).isEqualTo(DiagnosisCategory.DEPENDENCY_FAILURE_SUSPECTED);
    assertThat(result.confidence()).isBetween(0.0, 1.0);
    assertThat(result.supportingSignalIds()).containsExactly(signalId);
    assertThat(result.abstentionReason()).isNull();
  }

  @Test
  void abstainsWhenRulesTie() {
    DiagnosisResult result =
        engine.diagnose(List.of(signal("timeout from upstream"), signal("rollback")));

    assertThat(result.category()).isEqualTo(DiagnosisCategory.UNKNOWN);
    assertThat(result.abstentionReason()).isEqualTo("AMBIGUOUS_RULE_MATCH");
  }

  @Test
  void abstainsWhenThereAreNoSignals() {
    DiagnosisResult result = engine.diagnose(List.of());

    assertThat(result.category()).isEqualTo(DiagnosisCategory.INSUFFICIENT_EVIDENCE);
    assertThat(result.confidence()).isZero();
  }

  @Test
  void outputOrderingIsIndependentOfInputOrdering() {
    DiagnosisSignal first = signal("connection refused by dependency");
    DiagnosisSignal second = signal("timeout calling upstream");

    assertThat(engine.diagnose(List.of(first, second)))
        .isEqualTo(engine.diagnose(List.of(second, first)));
  }

  @Test
  void ignoresLowerScoringDiagnosisCategoriesWhenSelectingTheWinner() {
    DiagnosisResult result =
        engine.diagnose(
            List.of(
                signal("timeout from upstream"),
                signal("connection refused by dependency"),
                signal("rollback")));

    assertThat(result.category()).isEqualTo(DiagnosisCategory.DEPENDENCY_FAILURE_SUSPECTED);
  }

  @Test
  void rejectsMoreThanTheConfiguredSignalBound() {
    List<DiagnosisSignal> signals =
        java.util.stream.IntStream.range(0, DeterministicDiagnosisEngine.MAX_SIGNALS + 1)
            .mapToObj(ignored -> signal("status=failed"))
            .toList();

    assertThatThrownBy(() -> engine.diagnose(signals))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("diagnosis bound");
  }

  private static DiagnosisSignal signal(String text) {
    return new DiagnosisSignal(
        UUID.randomUUID(), UUID.randomUUID(), "test", Instant.parse("2026-08-14T10:00:00Z"), text);
  }
}
