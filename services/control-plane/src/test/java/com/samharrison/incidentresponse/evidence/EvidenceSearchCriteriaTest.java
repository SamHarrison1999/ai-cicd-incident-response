package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class EvidenceSearchCriteriaTest {

  @Test
  void trimsOptionalFiltersAndAcceptsBoundedLimit() {
    EvidenceSearchCriteria criteria =
        new EvidenceSearchCriteria(
            EvidenceKind.LOG_EXCERPT,
            "  github  ",
            "  timeout  ",
            Instant.parse("2026-08-14T10:00:00Z"),
            Instant.parse("2026-08-14T11:00:00Z"),
            25);

    assertThat(criteria.sourceSystem()).isEqualTo("github");
    assertThat(criteria.query()).isEqualTo("timeout");
    assertThat(criteria.limit()).isEqualTo(25);
  }

  @Test
  void rejectsUnboundedLimitAndReversedRange() {
    assertThatThrownBy(() -> new EvidenceSearchCriteria(null, null, null, null, null, 101))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new EvidenceSearchCriteria(
                    null,
                    null,
                    null,
                    Instant.parse("2026-08-14T11:00:00Z"),
                    Instant.parse("2026-08-14T10:00:00Z"),
                    10))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
