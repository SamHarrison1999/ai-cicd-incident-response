package com.samharrison.incidentresponse.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserAccountRepository;
import com.samharrison.incidentresponse.identity.UserStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceCoverageTest {

  private static final Instant NOW = Instant.parse("2026-08-16T12:00:00Z");

  @Mock private UserAccountRepository userRepository;
  @Mock private RefreshTokenSessionRepository sessionRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private OpaqueTokenGenerator tokenGenerator;
  @Mock private JwtAccessTokenService accessTokenService;

  private AuthenticationService service;
  private UserAccount user;

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
            false,
            12);
    service =
        new AuthenticationService(
            userRepository,
            sessionRepository,
            passwordEncoder,
            tokenGenerator,
            new TokenHasher(),
            accessTokenService,
            properties,
            Clock.fixed(NOW, ZoneOffset.UTC));
    user =
        new UserAccount(
            UUID.randomUUID(),
            "sam@example.com",
            "sam@example.com",
            "Sam",
            "encoded-password",
            UserStatus.ACTIVE);
  }

  @Test
  void activeLoginCreatesSessionAndReturnsTokens() {
    when(userRepository.findByNormalisedEmail("sam@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", user.getPasswordHash())).thenReturn(true);
    when(tokenGenerator.generate()).thenReturn("refresh-token");
    when(accessTokenService.issue(any(), any()))
        .thenReturn(new JwtAccessTokenService.AccessToken("access-token", 900));

    AuthenticationService.AuthenticatedSession result =
        service.login(" Sam@Example.COM ", "password");

    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");
    assertThat(result.userId()).isEqualTo(user.getId());
    verify(sessionRepository).save(any(RefreshTokenSession.class));
  }

  @Test
  void blankEmailIsRejectedBeforeRepositoryAccess() {
    assertThatThrownBy(() -> service.login("  ", "password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid email address or password.");

    verifyNoInteractions(userRepository);
  }

  @Test
  void blankRefreshTokenIsRejected() {
    assertThatThrownBy(() -> service.refresh(" "))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("The refresh token is invalid.");
  }

  @Test
  void unknownRefreshTokenIsRejected() {
    when(sessionRepository.findByTokenHash(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.refresh("unknown"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("The refresh token is invalid.");
  }

  @Test
  void replacedRefreshTokenRevokesItsFamily() {
    UUID familyId = UUID.randomUUID();
    RefreshTokenSession replaced = session("replaced", familyId, NOW.plusSeconds(600));
    replaced.rotateTo(UUID.randomUUID(), NOW.minusSeconds(10));
    RefreshTokenSession sibling = session("sibling", familyId, NOW.plusSeconds(600));
    when(sessionRepository.findByTokenHash(any())).thenReturn(Optional.of(replaced));
    when(sessionRepository.findAllByTokenFamilyId(familyId)).thenReturn(List.of(replaced, sibling));

    assertThatThrownBy(() -> service.refresh("reused"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("reuse was detected");

    verify(sessionRepository).saveAll(List.of(replaced, sibling));
    assertThat(sibling.getRevocationReason()).isEqualTo("REFRESH_TOKEN_REUSE");
  }

  @Test
  void expiredRefreshTokenIsRejected() {
    RefreshTokenSession expired = session("expired", UUID.randomUUID(), NOW.minusSeconds(1));
    when(sessionRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> service.refresh("expired"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("The refresh token is expired or revoked.");
  }

  @Test
  void disabledUserRefreshTokenIsRejected() {
    UserAccount disabled =
        new UserAccount(
            user.getId(),
            user.getEmail(),
            user.getNormalisedEmail(),
            user.getDisplayName(),
            user.getPasswordHash(),
            UserStatus.DISABLED);
    RefreshTokenSession session =
        new RefreshTokenSession(
            UUID.randomUUID(),
            disabled,
            UUID.randomUUID(),
            "disabled-hash",
            NOW.plusSeconds(600),
            NOW.minusSeconds(10));
    when(sessionRepository.findByTokenHash(any())).thenReturn(Optional.of(session));

    assertThatThrownBy(() -> service.refresh("disabled"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("The refresh token is expired or revoked.");
  }

  @Test
  void usableRefreshTokenRotatesAndCreatesReplacement() {
    UUID familyId = UUID.randomUUID();
    RefreshTokenSession current = session("current", familyId, NOW.plusSeconds(600));
    when(sessionRepository.findByTokenHash(any())).thenReturn(Optional.of(current));
    when(tokenGenerator.generate()).thenReturn("replacement-token");
    when(accessTokenService.issue(any(), any()))
        .thenReturn(new JwtAccessTokenService.AccessToken("replacement-access", 900));

    AuthenticationService.AuthenticatedSession replacement = service.refresh("current-token");

    assertThat(replacement.accessToken()).isEqualTo("replacement-access");
    assertThat(current.isReplaced()).isTrue();
    assertThat(current.getRevocationReason()).isEqualTo("ROTATED");
    verify(sessionRepository).save(current);
  }

  @Test
  void logoutIgnoresBlankAndUnknownTokens() {
    service.logout(null);
    service.logout(" ");
    when(sessionRepository.findByTokenHash(any())).thenReturn(Optional.empty());
    service.logout("unknown");

    verify(sessionRepository).findByTokenHash(any());
  }

  @Test
  void logoutRevokesTheWholeTokenFamily() {
    UUID familyId = UUID.randomUUID();
    RefreshTokenSession current = session("current", familyId, NOW.plusSeconds(600));
    RefreshTokenSession sibling = session("sibling", familyId, NOW.plusSeconds(600));
    when(sessionRepository.findByTokenHash(any())).thenReturn(Optional.of(current));
    when(sessionRepository.findAllByTokenFamilyId(familyId)).thenReturn(List.of(current, sibling));

    service.logout("current-token");

    verify(sessionRepository).saveAll(List.of(current, sibling));
    assertThat(current.getRevocationReason()).isEqualTo("USER_LOGOUT");
    assertThat(sibling.getRevocationReason()).isEqualTo("USER_LOGOUT");
  }

  private RefreshTokenSession session(String hash, UUID familyId, Instant expiresAt) {
    return new RefreshTokenSession(
        UUID.randomUUID(), user, familyId, hash, expiresAt, NOW.minusSeconds(10));
  }
}
