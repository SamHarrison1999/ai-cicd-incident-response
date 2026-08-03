package com.samharrison.incidentresponse.authentication;

import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserAccountRepository;
import com.samharrison.incidentresponse.identity.UserStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

  private static final String INVALID_CREDENTIALS = "Invalid email address or password.";

  private final UserAccountRepository userRepository;
  private final RefreshTokenSessionRepository sessionRepository;
  private final PasswordEncoder passwordEncoder;
  private final OpaqueTokenGenerator tokenGenerator;
  private final TokenHasher tokenHasher;
  private final JwtAccessTokenService accessTokenService;
  private final AuthenticationProperties properties;
  private final Clock clock;

  @Autowired
  public AuthenticationService(
      UserAccountRepository userRepository,
      RefreshTokenSessionRepository sessionRepository,
      PasswordEncoder passwordEncoder,
      OpaqueTokenGenerator tokenGenerator,
      TokenHasher tokenHasher,
      JwtAccessTokenService accessTokenService,
      AuthenticationProperties properties) {
    this(
        userRepository,
        sessionRepository,
        passwordEncoder,
        tokenGenerator,
        tokenHasher,
        accessTokenService,
        properties,
        Clock.systemUTC());
  }

  AuthenticationService(
      UserAccountRepository userRepository,
      RefreshTokenSessionRepository sessionRepository,
      PasswordEncoder passwordEncoder,
      OpaqueTokenGenerator tokenGenerator,
      TokenHasher tokenHasher,
      JwtAccessTokenService accessTokenService,
      AuthenticationProperties properties,
      Clock clock) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenGenerator = tokenGenerator;
    this.tokenHasher = tokenHasher;
    this.accessTokenService = accessTokenService;
    this.properties = properties;
    this.clock = clock;
  }

  @Transactional
  public RegisteredUser register(String email, String displayName, String password) {
    String normalisedEmail = normaliseEmail(email);

    if (password.length() < properties.passwordMinimumLength()) {
      throw new AuthenticationException(
          "PASSWORD_TOO_SHORT",
          "Password must contain at least " + properties.passwordMinimumLength() + " characters.");
    }

    if (userRepository.existsByNormalisedEmail(normalisedEmail)) {
      throw new DuplicateEmailException();
    }

    UserAccount user =
        new UserAccount(
            UUID.randomUUID(),
            email.trim(),
            normalisedEmail,
            displayName.trim(),
            passwordEncoder.encode(password),
            UserStatus.ACTIVE);

    UserAccount saved = userRepository.save(user);
    return new RegisteredUser(
        saved.getId(), saved.getEmail(), saved.getDisplayName(), saved.getStatus().name());
  }

  @Transactional
  public AuthenticatedSession login(String email, String password) {
    UserAccount user =
        userRepository
            .findByNormalisedEmail(normaliseEmail(email))
            .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
            .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
            .orElseThrow(
                () -> new AuthenticationException("INVALID_CREDENTIALS", INVALID_CREDENTIALS));

    return createSession(user, UUID.randomUUID());
  }

  @Transactional
  public AuthenticatedSession refresh(String rawRefreshToken) {
    Instant now = clock.instant();
    String tokenHash = tokenHasher.hash(requireToken(rawRefreshToken));

    RefreshTokenSession current =
        sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(
                () ->
                    new AuthenticationException(
                        "INVALID_REFRESH_TOKEN", "The refresh token is invalid."));

    if (current.isReplaced()) {
      revokeFamily(current.getTokenFamilyId(), "REFRESH_TOKEN_REUSE", now);
      throw new AuthenticationException(
          "REFRESH_TOKEN_REUSE", "Refresh token reuse was detected and the session was revoked.");
    }

    if (!current.isUsableAt(now) || current.getUser().getStatus() != UserStatus.ACTIVE) {
      throw new AuthenticationException(
          "INVALID_REFRESH_TOKEN", "The refresh token is expired or revoked.");
    }

    AuthenticatedSession replacement = createSession(current.getUser(), current.getTokenFamilyId());

    current.rotateTo(replacement.sessionId(), now);
    sessionRepository.save(current);
    return replacement;
  }

  @Transactional
  public void logout(String rawRefreshToken) {
    if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
      return;
    }

    sessionRepository
        .findByTokenHash(tokenHasher.hash(rawRefreshToken))
        .ifPresent(
            session -> revokeFamily(session.getTokenFamilyId(), "USER_LOGOUT", clock.instant()));
  }

  private AuthenticatedSession createSession(UserAccount user, UUID tokenFamilyId) {
    Instant now = clock.instant();
    UUID sessionId = UUID.randomUUID();
    String refreshToken = tokenGenerator.generate();

    RefreshTokenSession session =
        new RefreshTokenSession(
            sessionId,
            user,
            tokenFamilyId,
            tokenHasher.hash(refreshToken),
            now.plus(properties.refreshTokenTtl()),
            now);

    sessionRepository.save(session);
    JwtAccessTokenService.AccessToken accessToken = accessTokenService.issue(user, sessionId);

    return new AuthenticatedSession(
        sessionId,
        accessToken.value(),
        accessToken.expiresInSeconds(),
        refreshToken,
        user.getId(),
        user.getEmail(),
        user.getDisplayName());
  }

  private void revokeFamily(UUID tokenFamilyId, String reason, Instant revokedAt) {
    List<RefreshTokenSession> sessions = sessionRepository.findAllByTokenFamilyId(tokenFamilyId);
    sessions.forEach(session -> session.revoke(reason, revokedAt));
    sessionRepository.saveAll(sessions);
  }

  private String normaliseEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new AuthenticationException("INVALID_CREDENTIALS", INVALID_CREDENTIALS);
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String requireToken(String token) {
    if (token == null || token.isBlank()) {
      throw new AuthenticationException("INVALID_REFRESH_TOKEN", "The refresh token is invalid.");
    }
    return token;
  }

  public record RegisteredUser(UUID userId, String email, String displayName, String status) {}

  public record AuthenticatedSession(
      UUID sessionId,
      String accessToken,
      long expiresInSeconds,
      String refreshToken,
      UUID userId,
      String email,
      String displayName) {}
}
