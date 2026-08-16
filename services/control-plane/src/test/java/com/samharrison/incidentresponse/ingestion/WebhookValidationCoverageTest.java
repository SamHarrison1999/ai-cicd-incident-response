package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class WebhookValidationCoverageTest {

  @Test
  void coversWebhookValidationBoundaries() {
    WebhookIngestionService service = service();

    assertThatThrownBy(() -> invoke(service, "validateContentType", String.class, null))
        .isInstanceOf(WebhookIngestionException.class);
    assertThatThrownBy(
            () -> invoke(service, "validateContentType", String.class, "not a media type"))
        .isInstanceOf(WebhookIngestionException.class);
    assertThatThrownBy(() -> invoke(service, "validateContentType", String.class, "text/plain"))
        .isInstanceOf(WebhookIngestionException.class);

    assertThatThrownBy(
            () ->
                invoke(
                    service,
                    "validateSafeHeader",
                    String.class,
                    "bad value",
                    String.class,
                    "delivery ID",
                    int.class,
                    20))
        .isInstanceOf(WebhookIngestionException.class);
    assertThatThrownBy(
            () ->
                invoke(
                    service,
                    "requireHeader",
                    String.class,
                    "x".repeat(21),
                    String.class,
                    "delivery ID",
                    int.class,
                    20))
        .isInstanceOf(WebhookIngestionException.class);
    assertThatThrownBy(
            () ->
                invoke(
                    service,
                    "requireHeader",
                    String.class,
                    "bad\nvalue",
                    String.class,
                    "delivery ID",
                    int.class,
                    20))
        .isInstanceOf(WebhookIngestionException.class);

    assertThatThrownBy(
            () ->
                invoke(
                    service,
                    "readBoundedPayload",
                    InputStream.class,
                    new ByteArrayInputStream(new byte[] {1, 2}),
                    long.class,
                    3L,
                    int.class,
                    1))
        .isInstanceOf(WebhookIngestionException.class);
    assertThatThrownBy(
            () ->
                invoke(
                    service,
                    "readBoundedPayload",
                    InputStream.class,
                    new ByteArrayInputStream(new byte[0]),
                    long.class,
                    0L,
                    int.class,
                    10))
        .isInstanceOf(WebhookIngestionException.class);
    assertThatThrownBy(
            () ->
                invoke(
                    service,
                    "readBoundedPayload",
                    InputStream.class,
                    new InputStream() {
                      @Override
                      public int read() throws IOException {
                        throw new IOException("read failed");
                      }
                    },
                    long.class,
                    1L,
                    int.class,
                    10))
        .isInstanceOf(WebhookIngestionException.class);
    assertThatThrownBy(() -> invoke(service, "validateJson", byte[].class, "not-json".getBytes()))
        .isInstanceOf(WebhookIngestionException.class);
    assertThatThrownBy(
            () ->
                invoke(
                    service,
                    "validateTimestampTolerance",
                    Instant.class,
                    Instant.parse("2026-01-01T00:00:11Z"),
                    int.class,
                    10))
        .isInstanceOf(WebhookIngestionException.class);
  }

  @Test
  void rejectsMalformedSignatureAfterHeaderValidation() {
    EventSourceRepository sources = mock(EventSourceRepository.class);
    EventSource source = mock(EventSource.class);
    UUID sourceId = UUID.randomUUID();
    when(sources.findForWebhookIngestionById(sourceId)).thenReturn(Optional.of(source));
    when(source.isEnabled()).thenReturn(true);
    WebhookIngestionService service =
        new WebhookIngestionService(
            sources,
            mock(WebhookSecretResolver.class),
            mock(WebhookSignatureService.class),
            mock(WebhookDeliveryStore.class),
            mock(NormalisedEventProcessingService.class),
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    assertThatThrownBy(
            () ->
                service.ingest(
                    sourceId,
                    "delivery-1",
                    "workflow_run",
                    "2026-01-01T00:00:00Z",
                    "malformed",
                    "application/json",
                    2,
                    new ByteArrayInputStream("{}".getBytes())))
        .isInstanceOf(WebhookIngestionException.class);
  }

  @Test
  void convertsObjectMapperIoFailureToBoundedWebhookError() {
    try (MockedConstruction<ObjectMapper> ignored =
        mockConstruction(
            ObjectMapper.class,
            (mapper, context) ->
                when(mapper.readTree(any(byte[].class)))
                    .thenThrow(new IOException("read failed")))) {
      WebhookIngestionService service = service();
      assertThatThrownBy(() -> invoke(service, "validateJson", byte[].class, "{}".getBytes()))
          .isInstanceOf(WebhookIngestionException.class);
    }
  }

  private static WebhookIngestionService service() {
    EventSourceRepository sources = mock(EventSourceRepository.class);
    WebhookSecretResolver secrets = mock(WebhookSecretResolver.class);
    WebhookSignatureService signatures = mock(WebhookSignatureService.class);
    WebhookDeliveryStore deliveries = mock(WebhookDeliveryStore.class);
    NormalisedEventProcessingService processing = mock(NormalisedEventProcessingService.class);
    return new WebhookIngestionService(
        sources,
        secrets,
        signatures,
        deliveries,
        processing,
        Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
  }

  private static Object invoke(Object target, String name, Object... values) throws Throwable {
    int parameterCount = values.length / 2;
    Class<?>[] types = new Class<?>[parameterCount];
    Object[] arguments = new Object[parameterCount];
    for (int index = 0; index < parameterCount; index++) {
      types[index] = (Class<?>) values[index * 2];
      arguments[index] = values[index * 2 + 1];
    }
    Method method = target.getClass().getDeclaredMethod(name, types);
    method.setAccessible(true);
    try {
      return method.invoke(target, arguments);
    } catch (InvocationTargetException exception) {
      throw exception.getCause();
    }
  }
}
