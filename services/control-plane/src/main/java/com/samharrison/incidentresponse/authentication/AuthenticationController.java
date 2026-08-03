package com.samharrison.incidentresponse.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final AuthenticationProperties properties;

  public AuthenticationController(
      AuthenticationService authenticationService, AuthenticationProperties properties) {
    this.authenticationService = authenticationService;
    this.properties = properties;
  }

  @PostMapping("/register")
  ResponseEntity<RegisteredUserResponse> register(@Valid @RequestBody RegisterRequest request) {
    AuthenticationService.RegisteredUser registered =
        authenticationService.register(request.email(), request.displayName(), request.password());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new RegisteredUserResponse(
                registered.userId(),
                registered.email(),
                registered.displayName(),
                registered.status()));
  }

  @PostMapping("/login")
  ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
    return sessionResponse(authenticationService.login(request.email(), request.password()));
  }

  @PostMapping("/refresh")
  ResponseEntity<AuthenticationResponse> refresh(
      @CookieValue(name = "incident_refresh", required = false) String refreshToken,
      HttpServletRequest request) {
    String resolvedToken = findCookie(request, properties.refreshCookieName(), refreshToken);
    return sessionResponse(authenticationService.refresh(resolvedToken));
  }

  @PostMapping("/logout")
  ResponseEntity<Void> logout(
      @CookieValue(name = "incident_refresh", required = false) String refreshToken,
      HttpServletRequest request) {
    String resolvedToken = findCookie(request, properties.refreshCookieName(), refreshToken);
    authenticationService.logout(resolvedToken);

    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie().toString())
        .build();
  }

  private ResponseEntity<AuthenticationResponse> sessionResponse(
      AuthenticationService.AuthenticatedSession session) {
    AuthenticationResponse body =
        new AuthenticationResponse(
            session.accessToken(),
            "Bearer",
            session.expiresInSeconds(),
            new CurrentUserResponse(session.userId(), session.email(), session.displayName()));

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie(session.refreshToken()).toString())
        .body(body);
  }

  private ResponseCookie refreshCookie(String token) {
    return ResponseCookie.from(properties.refreshCookieName(), token)
        .httpOnly(true)
        .secure(properties.secureCookies())
        .sameSite("Strict")
        .path("/api/v1/auth")
        .maxAge(properties.refreshTokenTtl())
        .build();
  }

  private ResponseCookie expiredRefreshCookie() {
    return ResponseCookie.from(properties.refreshCookieName(), "")
        .httpOnly(true)
        .secure(properties.secureCookies())
        .sameSite("Strict")
        .path("/api/v1/auth")
        .maxAge(Duration.ZERO)
        .build();
  }

  private String findCookie(HttpServletRequest request, String cookieName, String fallbackValue) {
    if (request.getCookies() == null) {
      return fallbackValue;
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> cookieName.equals(cookie.getName()))
        .map(cookie -> cookie.getValue())
        .findFirst()
        .orElse(fallbackValue);
  }

  public record RegisterRequest(
      @NotBlank @Email @Size(max = 320) String email,
      @NotBlank @Size(max = 120) String displayName,
      @NotBlank @Size(max = 200) String password) {}

  public record LoginRequest(
      @NotBlank @Email @Size(max = 320) String email, @NotBlank @Size(max = 200) String password) {}

  public record RegisteredUserResponse(
      UUID userId, String email, String displayName, String status) {}

  public record CurrentUserResponse(UUID userId, String email, String displayName) {}

  public record AuthenticationResponse(
      String accessToken, String tokenType, long expiresInSeconds, CurrentUserResponse user) {}
}
