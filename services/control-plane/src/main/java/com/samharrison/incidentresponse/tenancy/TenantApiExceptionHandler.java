package com.samharrison.incidentresponse.tenancy;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TenantApiExceptionHandler {

  @ExceptionHandler(TenantAccessException.class)
  ResponseEntity<ApiError> handleTenantAccess(
      TenantAccessException exception, HttpServletRequest request) {
    return response(exception.getStatus(), exception.getCode(), exception.getMessage(), request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> handleConflict(
      DataIntegrityViolationException exception, HttpServletRequest request) {
    return response(
        HttpStatus.CONFLICT,
        "RESOURCE_CONFLICT",
        "The requested identifier is already in use.",
        request);
  }

  private ResponseEntity<ApiError> response(
      HttpStatus status, String code, String message, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(
            new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                MDC.get("correlationId")));
  }

  public record ApiError(
      Instant timestamp,
      int status,
      String code,
      String message,
      String path,
      String correlationId) {}
}
