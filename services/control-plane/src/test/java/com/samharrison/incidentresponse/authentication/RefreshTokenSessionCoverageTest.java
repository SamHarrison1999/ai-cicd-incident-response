package com.samharrison.incidentresponse.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.samharrison.incidentresponse.identity.UserAccount;
import com.samharrison.incidentresponse.identity.UserStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenSessionCoverageTest {

  private static final Instant NOW = Instant.parse("2026-08-16T12:00:00Z");

  @Test
  void coversRevocationReplacementAndUsabilityBranches() {
    RefreshTokenSession active = session(NOW.plusSeconds(60));
    assertThat(active.isRevoked()).isFalse();
    assertThat(active.isUsableAt(NOW)).isTrue();
    assertThat(active.isUsableAt(NOW.plusSeconds(60))).isFalse();

    RefreshTokenSession revoked = session(NOW.plusSeconds(60));
    revoked.revoke("USER_LOGOUT", NOW);
    assertThat(revoked.isRevoked()).isTrue();
    assertThat(revoked.isUsableAt(NOW)).isFalse();

    RefreshTokenSession replaced = session(NOW.plusSeconds(60));
    replaced.rotateTo(UUID.randomUUID(), NOW);
    assertThat(replaced.isReplaced()).isTrue();
    assertThat(replaced.isRevoked()).isTrue();
  }

  private static RefreshTokenSession session(Instant expiresAt) {
    UserAccount user =
        new UserAccount(
            UUID.randomUUID(),
            "coverage@example.com",
            "coverage@example.com",
            "Coverage",
            "hash",
            UserStatus.ACTIVE);
    return new RefreshTokenSession(
        UUID.randomUUID(), user, UUID.randomUUID(), "token-hash", expiresAt, NOW);
  }
}
