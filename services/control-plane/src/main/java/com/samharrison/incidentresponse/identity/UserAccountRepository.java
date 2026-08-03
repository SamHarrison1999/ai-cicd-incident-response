package com.samharrison.incidentresponse.identity;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

  Optional<UserAccount> findByNormalisedEmail(String normalisedEmail);

  boolean existsByNormalisedEmail(String normalisedEmail);
}
