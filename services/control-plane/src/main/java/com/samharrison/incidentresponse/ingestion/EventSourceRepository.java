package com.samharrison.incidentresponse.ingestion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventSourceRepository extends JpaRepository<EventSource, UUID> {

  Optional<EventSource> findByIdAndOrganisationId(UUID id, UUID organisationId);

  List<EventSource> findAllByProjectIdAndOrganisationIdOrderByDisplayNameAsc(
      UUID projectId, UUID organisationId);

  boolean existsByProjectIdAndDisplayName(UUID projectId, String displayName);
}
