package com.samharrison.incidentresponse.organisation;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

  Optional<Organisation> findBySlug(String slug);

  boolean existsBySlug(String slug);
}
