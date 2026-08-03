package com.samharrison.incidentresponse.authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, UUID> {

  Optional<RefreshTokenSession> findByTokenHash(String tokenHash);

  List<RefreshTokenSession> findAllByTokenFamilyId(UUID tokenFamilyId);
}
