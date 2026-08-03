package com.samharrison.incidentresponse.organisation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

  Optional<Organisation> findBySlug(String slug);

  boolean existsBySlug(String slug);

  List<Organisation> findAllByIdInOrderByNameAsc(Collection<UUID> ids);
}
