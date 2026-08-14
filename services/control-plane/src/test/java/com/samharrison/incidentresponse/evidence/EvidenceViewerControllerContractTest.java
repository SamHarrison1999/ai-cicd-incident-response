package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceViewerControllerContractTest {

  @Test
  void viewerResponseContainsBoundedRedactedContentAndLinks() {
    EvidenceViewerController.EvidenceViewerResponse response =
        new EvidenceViewerController.EvidenceViewerResponse(
            UUID.randomUUID(),
            "LOG_EXCERPT",
            "STANDARD",
            "github",
            "delivery-1",
            Instant.parse("2026-08-14T12:00:00Z"),
            Instant.parse("2026-08-14T12:01:00Z"),
            "a".repeat(64),
            "token=[REDACTED]",
            1,
            List.of(UUID.randomUUID()),
            List.of(UUID.randomUUID()));

    assertThat(response.content()).doesNotContain("token=raw-secret");
    assertThat(response.content()).hasSizeLessThanOrEqualTo(EvidenceRedactor.MAX_CHARS);
    assertThat(response.incidentIds()).hasSize(1);
    assertThat(response.eventIds()).hasSize(1);
  }
}
