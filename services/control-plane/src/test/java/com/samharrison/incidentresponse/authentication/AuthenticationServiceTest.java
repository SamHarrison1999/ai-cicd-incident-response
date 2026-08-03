package com.samharrison.incidentresponse.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserAccountRepository;
import com.samharrison.incidentresponse.identity.UserStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  private static final Instant NOW = Instant.parse("2026-08-03T12:00:00Z");

  @Mock private UserAccountRepository userRepository;
  @Mock private RefreshTokenSessionRepository sessionRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private OpaqueTokenGenerator tokenGenerator;
  @Mock private JwtAccessTokenService accessTokenService;

  private final TokenHasher tokenHasher = new TokenHasher();
  private AuthenticationService service;

  @BeforeEach
  void setUp() {
    AuthenticationProperties properties =
        new AuthenticationProperties(
            "incident-response-control-plane",
            "incident-response-web",
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
            tokenHasher,
            accessTokenService,
            properties,
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void registrationNormalisesEmailAndHashesPassword() {
    when(userRepository.existsByNormalisedEmail("sam@example.com")).thenReturn(false);
    when(passwordEncoder.encode("correct-horse-battery")).thenReturn("encoded-password");
    when(userRepository.save(any(UserAccount.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AuthenticationService.RegisteredUser registered =
        service.register(" Sam@Example.COM ", " Sam Harrison ", "correct-horse-battery");

    assertThat(registered.email()).isEqualTo("Sam@Example.COM");
    assertThat(registered.displayName()).isEqualTo("Sam Harrison");
    verify(passwordEncoder).encode("correct-horse-battery");
  }

  @Test
  void duplicateEmailIsRejectedBeforePasswordHashing() {
    when(userRepository.existsByNormalisedEmail("sam@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.register("sam@example.com", "Sam", "correct-horse-battery"))
        .isInstanceOf(DuplicateEmailException.class);
  }

  @Test
  void loginReturnsGenericFailureForUnknownEmail() {
    when(userRepository.findByNormalisedEmail("unknown@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.login("unknown@example.com", "password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid email address or password.");
  }

  @Test
  void disabledUserCannotLogin() {
    UserAccount disabled =
        new UserAccount(
            UUID.randomUUID(),
            "sam@example.com",
            "sam@example.com",
            "Sam",
            "encoded",
            UserStatus.DISABLED);
    when(userRepository.findByNormalisedEmail("sam@example.com")).thenReturn(Optional.of(disabled));

    assertThatThrownBy(() -> service.login("sam@example.com", "password"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid email address or password.");
  }

  @Test
  void shortPasswordIsRejected() {
    assertThatThrownBy(() -> service.register("sam@example.com", "Sam", "too-short"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessageContaining("at least 12 characters");
  }
}
