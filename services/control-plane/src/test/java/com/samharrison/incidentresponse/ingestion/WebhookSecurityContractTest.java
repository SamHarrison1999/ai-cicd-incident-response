package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WebhookSecurityContractTest {
  private static final byte[] SECRET = "security-contract-secret".getBytes(StandardCharsets.UTF_8);
  private static final byte[] PAYLOAD = "{\"event\":\"safe\"}".getBytes(StandardCharsets.UTF_8);

  @Test
  void metadataAndExactPayloadAreBoundToTheSignature() {
    WebhookSignatureService service = new WebhookSignatureService();
    String timestamp = "2026-08-13T12:00:00Z";
    String signature =
        service.calculateSignature("delivery-1", "workflow_run", timestamp, PAYLOAD, SECRET);

    assertThat(service.verify("delivery-1", "workflow_run", timestamp, PAYLOAD, SECRET, signature))
        .isTrue();
    assertThat(service.verify("delivery-2", "workflow_run", timestamp, PAYLOAD, SECRET, signature))
        .isFalse();
    assertThat(service.verify("delivery-1", "check_suite", timestamp, PAYLOAD, SECRET, signature))
        .isFalse();
    assertThat(
            service.verify(
                "delivery-1",
                "workflow_run",
                timestamp,
                "{\"event\":\"changed\"}".getBytes(StandardCharsets.UTF_8),
                SECRET,
                signature))
        .isFalse();
    assertThat(
            service.verify(
                "delivery-1", "workflow_run", "2026-08-13T12:01:00Z", PAYLOAD, SECRET, signature))
        .isFalse();
  }

  @Test
  void rejectsMalformedSignatureWireFormat() {
    assertThat(new WebhookSignatureService().hasValidFormat("sha256=not-hex")).isFalse();
    assertThat(new WebhookSignatureService().hasValidFormat("hmac=0123")).isFalse();
  }
}
