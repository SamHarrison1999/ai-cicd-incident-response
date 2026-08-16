package com.samharrison.incidentresponse.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

class CurrentUserProviderCoverageTest {

  @Test
  void acceptsAuthenticatedJwtSubject() {
    UUID userId = UUID.randomUUID();
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal())
        .thenReturn(
            Jwt.withTokenValue("token").header("alg", "none").subject(userId.toString()).build());

    assertThat(new CurrentUserProvider().requireUserId(authentication)).isEqualTo(userId);
  }

  @Test
  void rejectsMissingUnauthenticatedNonJwtAndMalformedSubjects() {
    CurrentUserProvider provider = new CurrentUserProvider();
    Authentication unauthenticated = mock(Authentication.class);
    when(unauthenticated.isAuthenticated()).thenReturn(false);
    Authentication nonJwt = mock(Authentication.class);
    when(nonJwt.isAuthenticated()).thenReturn(true);
    when(nonJwt.getPrincipal()).thenReturn("principal");
    Authentication malformed = mock(Authentication.class);
    when(malformed.isAuthenticated()).thenReturn(true);
    when(malformed.getPrincipal())
        .thenReturn(Jwt.withTokenValue("token").header("alg", "none").subject("bad").build());

    assertThatThrownBy(() -> provider.requireUserId(null))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("A valid access token is required.");
    assertThatThrownBy(() -> provider.requireUserId(unauthenticated))
        .isInstanceOf(TenantAccessException.class);
    assertThatThrownBy(() -> provider.requireUserId(nonJwt))
        .isInstanceOf(TenantAccessException.class);
    assertThatThrownBy(() -> provider.requireUserId(malformed))
        .isInstanceOf(TenantAccessException.class)
        .hasMessage("The access token subject is invalid.");
  }
}
