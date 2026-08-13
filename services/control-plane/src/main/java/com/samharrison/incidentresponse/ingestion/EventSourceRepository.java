package com.samharrison.incidentresponse.ingestion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventSourceRepository extends JpaRepository<EventSource, UUID> {

  Optional<EventSource> findByIdAndOrganisationId(UUID id, UUID organisationId);

  @EntityGraph(attributePaths = {"organisation", "project"})
  @Query("select source from EventSource source where source.id = :id")
  Optional<EventSource> findForWebhookIngestionById(@Param("id") UUID id);

  List<EventSource> findAllByProjectIdAndOrganisationIdOrderByDisplayNameAsc(
      UUID projectId, UUID organisationId);

  boolean existsByProjectIdAndDisplayName(UUID projectId, String displayName);
}
