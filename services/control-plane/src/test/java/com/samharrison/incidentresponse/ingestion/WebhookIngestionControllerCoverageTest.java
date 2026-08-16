package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class WebhookIngestionControllerCoverageTest {
  @Test
  void convertsUnreadablePayloadsToBoundedWebhookErrors() throws Exception {
    WebhookIngestionService service = mock(WebhookIngestionService.class);
    MockHttpServletRequest request = mock(MockHttpServletRequest.class);
    when(request.getContentType()).thenReturn("application/json");
    when(request.getContentLengthLong()).thenReturn(1L);
    when(request.getInputStream()).thenThrow(new IOException("read failed"));

    assertThatThrownBy(
            () ->
                new WebhookIngestionController(service)
                    .ingest(
                        UUID.randomUUID(),
                        "delivery",
                        "workflow_run",
                        Instant.now().toString(),
                        "sha256=" + "a".repeat(64),
                        request))
        .isInstanceOf(WebhookIngestionException.class)
        .hasMessage("The webhook payload could not be read.");
  }

  @Test
  void passesReadablePayloadToIngestionService() throws Exception {
    WebhookIngestionService service = mock(WebhookIngestionService.class);
    MockHttpServletRequest request = new MockHttpServletRequest();
    UUID sourceId = UUID.randomUUID();
    WebhookAcceptanceResponse response =
        new WebhookAcceptanceResponse(
            UUID.randomUUID(), false, WebhookDeliveryStatus.RECEIVED, Instant.now());
    request.setContentType("application/json");
    request.setContent("{}".getBytes());
    when(service.ingest(any(), any(), any(), any(), any(), any(), anyLong(), any()))
        .thenReturn(response);

    new WebhookIngestionController(service)
        .ingest(
            sourceId,
            "delivery",
            "workflow_run",
            Instant.now().toString(),
            "sha256=" + "a".repeat(64),
            request);
  }
}
