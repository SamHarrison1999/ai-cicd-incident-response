package com.samharrison.incidentresponse.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @Test
  void preservesValidCorrelationId() {
    String result = filter.resolveCorrelationId("pipeline-run:123");

    assertThat(result).isEqualTo("pipeline-run:123");
  }

  @Test
  void generatesCorrelationIdWhenHeaderIsMissing() {
    String result = filter.resolveCorrelationId(null);

    assertThatCodeIsUuid(result);
  }

  @Test
  void replacesUnsafeCorrelationId() {
    String result = filter.resolveCorrelationId("unsafe value\ninjected");

    assertThatCodeIsUuid(result);
  }

  @Test
  void replacesOverlongCorrelationId() {
    assertThatCodeIsUuid(filter.resolveCorrelationId("x".repeat(129)));
  }

  private static void assertThatCodeIsUuid(String value) {
    assertThat(UUID.fromString(value)).isNotNull();
  }
}
