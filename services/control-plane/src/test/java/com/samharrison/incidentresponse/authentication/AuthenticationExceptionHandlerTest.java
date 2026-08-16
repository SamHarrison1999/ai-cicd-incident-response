package com.samharrison.incidentresponse.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class AuthenticationExceptionHandlerTest {

  private final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();

  @Test
  void authenticationErrorsReturnBoundedMessagesWithoutCredentials() {
    var request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
    var response =
        handler.handleAuthentication(
            new AuthenticationException(
                "INVALID_CREDENTIALS", "Invalid email address or password."),
            request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
    assertThat(response.getBody().message()).doesNotContain("correct-horse-battery");
    assertThat(response.getBody().path()).isEqualTo("/api/v1/auth/login");
  }

  @Test
  void duplicateEmailErrorsDoNotEchoTheEmailAddress() {
    var request = new MockHttpServletRequest("POST", "/api/v1/auth/register");
    var response = handler.handleDuplicateEmail(new DuplicateEmailException(), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().code()).isEqualTo("EMAIL_ALREADY_REGISTERED");
    assertThat(response.getBody().message()).doesNotContain("@");
  }

  @Test
  void validationErrorsUseFallbackMessagesAndRetainTheFirstDuplicate() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError first = mock(FieldError.class);
    FieldError second = mock(FieldError.class);
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(first.getField()).thenReturn("email");
    when(first.getDefaultMessage()).thenReturn(null);
    when(second.getField()).thenReturn("email");
    when(second.getDefaultMessage()).thenReturn("A second message");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(first, second));
    when(exception.getBindingResult()).thenReturn(bindingResult);

    var response =
        handler.handleValidation(exception, new MockHttpServletRequest("POST", "/validate"));

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().fieldErrors()).containsEntry("email", "Invalid value");
  }
}
