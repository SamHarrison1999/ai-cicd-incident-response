package com.samharrison.incidentresponse.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerCoverageTest {

  @Mock private AuthenticationService authenticationService;
  @Mock private HttpServletRequest request;

  private AuthenticationController controller;
  private AuthenticationService.AuthenticatedSession session;

  @BeforeEach
  void setUp() {
    AuthenticationProperties properties =
        new AuthenticationProperties(
            "issuer",
            "audience",
            "local-development-signing-secret-change-me",
            Duration.ofMinutes(15),
            Duration.ofDays(7),
            "incident_refresh",
            "/api/v1/auth",
            false,
            12);
    controller = new AuthenticationController(authenticationService, properties);
    session =
        new AuthenticationService.AuthenticatedSession(
            UUID.randomUUID(),
            "access-token",
            900,
            "refresh-token",
            UUID.randomUUID(),
            "sam@example.com",
            "Sam");
  }

  @Test
  void registerReturnsCreatedUser() {
    UUID userId = UUID.randomUUID();
    when(authenticationService.register("sam@example.com", "Sam", "password"))
        .thenReturn(
            new AuthenticationService.RegisteredUser(userId, "sam@example.com", "Sam", "ACTIVE"));

    var response =
        controller.register(
            new AuthenticationController.RegisterRequest("sam@example.com", "Sam", "password"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().userId()).isEqualTo(userId);
  }

  @Test
  void loginReturnsBearerResponseAndRefreshCookie() {
    when(authenticationService.login("sam@example.com", "password")).thenReturn(session);

    var response =
        controller.login(new AuthenticationController.LoginRequest("sam@example.com", "password"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().accessToken()).isEqualTo("access-token");
    assertThat(response.getBody().tokenType()).isEqualTo("Bearer");
    assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
        .contains("incident_refresh=refresh-token")
        .contains("Path=/api/v1/auth");
  }

  @Test
  void refreshUsesMatchingCookieWhenPresent() {
    when(request.getCookies())
        .thenReturn(
            new Cookie[] {
              new Cookie("other", "ignored"), new Cookie("incident_refresh", "cookie-token")
            });
    when(authenticationService.refresh("cookie-token")).thenReturn(session);

    var response = controller.refresh("fallback-token", request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(authenticationService).refresh("cookie-token");
  }

  @Test
  void refreshFallsBackWhenCookieArrayIsAbsentOrDoesNotMatch() {
    when(authenticationService.refresh("fallback-token")).thenReturn(session);

    controller.refresh("fallback-token", request);
    when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("other", "ignored")});
    controller.refresh("fallback-token", request);

    verify(authenticationService, org.mockito.Mockito.times(2)).refresh("fallback-token");
  }

  @Test
  void logoutReturnsNoContentAndExpiresRefreshCookie() {
    when(request.getCookies()).thenReturn(null);

    var response = controller.logout("fallback-token", request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
        .contains("incident_refresh=")
        .contains("Max-Age=0");
    verify(authenticationService).logout("fallback-token");
  }
}
