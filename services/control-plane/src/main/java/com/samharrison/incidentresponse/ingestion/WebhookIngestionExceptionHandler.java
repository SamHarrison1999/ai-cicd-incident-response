package com.samharrison.incidentresponse.ingestion;

import com.samharrison.incidentresponse.common.web.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = WebhookIngestionController.class)
public class WebhookIngestionExceptionHandler {

  @ExceptionHandler(WebhookIngestionException.class)
  ResponseEntity<ApiError> handleWebhookIngestion(
      WebhookIngestionException exception, HttpServletRequest request) {
    return ResponseEntity.status(exception.getStatus())
        .body(
            new ApiError(
                Instant.now(),
                exception.getStatus().value(),
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                MDC.get(CorrelationIdFilter.MDC_KEY)));
  }

  public record ApiError(
      Instant timestamp,
      int status,
      String code,
      String message,
      String path,
      String correlationId) {}
}
