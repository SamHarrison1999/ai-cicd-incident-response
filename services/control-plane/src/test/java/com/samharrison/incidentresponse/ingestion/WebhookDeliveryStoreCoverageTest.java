package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

class WebhookDeliveryStoreCoverageTest {

  @Test
  void rejectsAnInsertThatCannotBeReadBack() {
    JdbcClient jdbc = mock(JdbcClient.class, RETURNS_DEEP_STUBS);
    when(jdbc.sql(anyString()).params(anyMap()).update()).thenReturn(1);
    WebhookDeliveryRepository repository = mock(WebhookDeliveryRepository.class);
    when(repository.findByEventSourceIdAndProviderDeliveryId(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(Optional.empty());

    WebhookDeliveryStore store = new WebhookDeliveryStore(jdbc, repository);
    Organisation organisation = new Organisation(UUID.randomUUID(), "Platform", "platform");
    Project project =
        new Project(
            UUID.randomUUID(),
            organisation,
            "Project",
            "project",
            "Coverage project",
            ProjectStatus.ACTIVE);
    EventSource source =
        new EventSource(
            UUID.randomUUID(),
            organisation,
            project,
            EventProvider.GITHUB_ACTIONS,
            "GitHub",
            EventSourceStatus.ENABLED,
            "secret-ref",
            SignatureAlgorithm.HMAC_SHA256,
            300,
            1024);

    assertThatThrownBy(
            () ->
                store.store(
                    source,
                    "delivery-1",
                    "workflow_run",
                    "a".repeat(64),
                    Instant.parse("2026-08-16T10:00:00Z"),
                    Instant.parse("2026-08-16T10:00:01Z")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("The webhook delivery insert completed without a readable record.");
  }
}
