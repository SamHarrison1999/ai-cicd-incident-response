package com.samharrison.incidentresponse.authentication;

import com.samharrison.incidentresponse.identity.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "refresh_token_sessions")
public class RefreshTokenSession {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserAccount user;

  @Column(name = "token_family_id", nullable = false)
  private UUID tokenFamilyId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "replaced_by_session_id")
  private UUID replacedBySessionId;

  @Column(name = "revocation_reason", length = 120)
  private String revocationReason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "last_used_at")
  private Instant lastUsedAt;

  protected RefreshTokenSession() {}

  public RefreshTokenSession(
      UUID id,
      UserAccount user,
      UUID tokenFamilyId,
      String tokenHash,
      Instant expiresAt,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.user = Objects.requireNonNull(user);
    this.tokenFamilyId = Objects.requireNonNull(tokenFamilyId);
    this.tokenHash = requireText(tokenHash, "tokenHash");
    this.expiresAt = Objects.requireNonNull(expiresAt);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public UUID getId() {
    return id;
  }

  public UserAccount getUser() {
    return user;
  }

  public UUID getTokenFamilyId() {
    return tokenFamilyId;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getRevokedAt() {
    return revokedAt;
  }

  public UUID getReplacedBySessionId() {
    return replacedBySessionId;
  }

  public String getRevocationReason() {
    return revocationReason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getLastUsedAt() {
    return lastUsedAt;
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }
}
