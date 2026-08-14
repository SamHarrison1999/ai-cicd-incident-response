package com.samharrison.incidentresponse.diagnosis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DiagnosisSecurityContractTest {

  @Test
  void diagnosisProjectionDoesNotExposeRawEvidenceFields() {
    assertThat(
            Arrays.stream(DiagnosisResult.class.getRecordComponents())
                .map(component -> component.getName())
                .toList())
        .doesNotContain("content", "rawContent", "secret", "token", "signature");
  }

  @Test
  void diagnosisCategoriesRemainBoundedHypotheses() {
    assertThat(DiagnosisCategory.DEPENDENCY_FAILURE_SUSPECTED.name()).endsWith("_SUSPECTED");
    assertThat(DiagnosisCategory.UNKNOWN)
        .isNotEqualTo(DiagnosisCategory.DEPENDENCY_FAILURE_SUSPECTED);
  }
}
