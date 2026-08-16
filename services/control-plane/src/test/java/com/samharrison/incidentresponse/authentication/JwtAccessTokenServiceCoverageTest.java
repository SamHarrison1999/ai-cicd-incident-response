package com.samharrison.incidentresponse.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.identity.UserAccount;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

class JwtAccessTokenServiceCoverageTest {
  @Test
  void issuesClaimsWithConfiguredLifetime() {
    JwtEncoder encoder = mock(JwtEncoder.class);
    AuthenticationProperties properties =
        new AuthenticationProperties(
            "issuer",
            "audience",
            "a".repeat(32),
            Duration.ofMinutes(15),
            Duration.ofDays(1),
            "refresh",
            true,
            8);
    Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
    Jwt encoded =
        Jwt.withTokenValue("encoded-token").header("alg", "HS256").subject("subject").build();
    when(encoder.encode(any())).thenReturn(encoded);
    UserAccount user =
        new UserAccount(
            UUID.randomUUID(),
            "user@example.test",
            "user@example.test",
            "User",
            "password-hash",
            com.samharrison.incidentresponse.identity.UserStatus.ACTIVE);

    JwtAccessTokenService service =
        new JwtAccessTokenService(encoder, properties, Clock.fixed(issuedAt, ZoneOffset.UTC));
    JwtAccessTokenService.AccessToken token = service.issue(user, UUID.randomUUID());

    assertThat(token.value()).isEqualTo("encoded-token");
    assertThat(token.expiresInSeconds()).isEqualTo(Duration.ofMinutes(15).toSeconds());
  }
}
