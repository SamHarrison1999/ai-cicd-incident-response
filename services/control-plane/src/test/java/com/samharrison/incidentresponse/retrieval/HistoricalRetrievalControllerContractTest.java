package com.samharrison.incidentresponse.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HistoricalRetrievalControllerContractTest {

  @Test
  void responseIsBoundedToReviewableHistoricalMetadata() {
    HistoricalRetrievalService.HistoricalRetrievalItem item =
        new HistoricalRetrievalService.HistoricalRetrievalItem(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "EVIDENCE",
            UUID.randomUUID(),
            java.time.Instant.parse("2026-08-14T12:00:00Z"),
            "GITHUB_ACTIONS",
            "build",
            "production",
            "refs/heads/main",
            "abc123",
            "DEPENDENCY_FAILURE_SUSPECTED",
            "bounded summary",
            "provider and diagnosis dimensions matched",
            "evidence:source-1");

    HistoricalRetrievalController.HistoricalRetrievalResponse response =
        new HistoricalRetrievalController.HistoricalRetrievalResponse(
            List.of(item), "cursor", true);

    assertThat(response.items()).hasSize(1);
    assertThat(response.items().getFirst().provenanceReference()).isEqualTo("evidence:source-1");
  }
}
