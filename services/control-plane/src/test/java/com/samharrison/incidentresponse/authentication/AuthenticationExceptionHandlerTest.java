package com.samharrison.incidentresponse.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

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
}
