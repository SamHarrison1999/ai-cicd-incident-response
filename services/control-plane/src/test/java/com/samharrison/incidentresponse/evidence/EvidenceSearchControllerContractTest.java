package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceSearchControllerContractTest {

  @Test
  void searchResponseContainsMetadataButNotRawContent() {
    EvidenceController.EvidenceResponse response =
        new EvidenceController.EvidenceResponse(
            UUID.randomUUID(),
            "LOG_EXCERPT",
            "STANDARD",
            "github",
            "delivery-123",
            Instant.parse("2026-08-14T12:00:00Z"),
            Instant.parse("2026-08-14T12:01:00Z"),
            "a".repeat(64),
            2);

    EvidenceController.EvidenceSearchResponse page =
        new EvidenceController.EvidenceSearchResponse(List.of(response), "next-cursor");

    assertThat(page.items()).hasSize(1);
    assertThat(page.items().get(0).contentHash()).hasSize(64);
    assertThat(page.items().get(0).sourceReference()).doesNotContain("secret", "payload");
  }
}
