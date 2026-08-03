package com.samharrison.incidentresponse.authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthenticationExceptionHandler {

  @ExceptionHandler(DuplicateEmailException.class)
  ResponseEntity<ApiError> handleDuplicateEmail(
      DuplicateEmailException exception, HttpServletRequest request) {
    return response(
        HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", exception.getMessage(), request);
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<ApiError> handleAuthentication(
      AuthenticationException exception, HttpServletRequest request) {
    return response(
        HttpStatus.UNAUTHORIZED, exception.getErrorCode(), exception.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    Map<String, String> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    field -> field.getField(),
                    field ->
                        field.getDefaultMessage() == null
                            ? "Invalid value"
                            : field.getDefaultMessage(),
                    (first, second) -> first));

    ApiError error =
        new ApiError(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            "Request validation failed.",
            request.getRequestURI(),
            MDC.get("correlationId"),
            fieldErrors);
    return ResponseEntity.badRequest().body(error);
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
                MDC.get("correlationId"),
                null));
  }

  public record ApiError(
      Instant timestamp,
      int status,
      String code,
      String message,
      String path,
      String correlationId,
      Map<String, String> fieldErrors) {}
}
