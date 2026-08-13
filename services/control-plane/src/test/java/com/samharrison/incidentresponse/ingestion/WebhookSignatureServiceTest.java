package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WebhookSignatureServiceTest {

  private static final byte[] SECRET = "test-signing-secret".getBytes(StandardCharsets.UTF_8);
  private static final byte[] PAYLOAD = "{\"status\":\"failed\"}".getBytes(StandardCharsets.UTF_8);

  private final WebhookSignatureService signatureService = new WebhookSignatureService();

  @Test
  void producesStableVersionedSignature() {
    assertThat(
            signatureService.calculateSignature(
                "delivery-42", "workflow_run", "2026-08-13T12:00:00Z", PAYLOAD, SECRET))
        .isEqualTo("sha256=3b3d95a41d59f941bc5c6a5195b3e5ba68f5ff281a69eb83fe465f93245ef490");
  }

  @Test
  void verifiesMatchingSignature() {
    String signature =
        signatureService.calculateSignature(
            "delivery-42", "workflow_run", "2026-08-13T12:00:00Z", PAYLOAD, SECRET);

    assertThat(
            signatureService.verify(
                "delivery-42", "workflow_run", "2026-08-13T12:00:00Z", PAYLOAD, SECRET, signature))
        .isTrue();
  }

  @Test
  void signatureBindsDeliveryMetadata() {
    String signature =
        signatureService.calculateSignature(
            "delivery-42", "workflow_run", "2026-08-13T12:00:00Z", PAYLOAD, SECRET);

    assertThat(
            signatureService.verify(
                "delivery-43", "workflow_run", "2026-08-13T12:00:00Z", PAYLOAD, SECRET, signature))
        .isFalse();
  }
}
