package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WebhookDeliveryCoverageTest {

  @Test
  void completesAndRetriesFailedDeliveries() {
    Instant received = Instant.parse("2026-08-16T10:00:00Z");
    Instant completed = received.plusSeconds(10);
    WebhookDelivery delivery = newDelivery();

    delivery.markFailed("PROCESSING_FAILED", completed);
    delivery.markProcessingRetry();

    assertThat(delivery.getStatus()).isEqualTo(WebhookDeliveryStatus.PROCESSING_RETRY);
    assertThat(delivery.getOutcomeCode()).isNull();
    assertThat(delivery.getProcessedAt()).isNull();
  }

  @Test
  void rejectsRetryBeforeFailureAndInvalidPayloadHashes() {
    WebhookDelivery delivery = newDelivery();
    assertThatThrownBy(delivery::markProcessingRetry)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("only failed deliveries can be retried");
    assertThatThrownBy(() -> newDelivery("not-a-hash"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private static WebhookDelivery newDelivery() {
    return newDelivery("a".repeat(64));
  }

  private static WebhookDelivery newDelivery(String hash) {
    Instant now = Instant.parse("2026-08-16T10:00:00Z");
    return new WebhookDelivery(
        UUID.randomUUID(),
        mock(Organisation.class),
        mock(Project.class),
        mock(EventSource.class),
        "delivery-1",
        "workflow_run",
        hash,
        now,
        now);
  }
}
