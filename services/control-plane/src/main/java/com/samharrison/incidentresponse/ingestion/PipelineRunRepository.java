package com.samharrison.incidentresponse.ingestion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, UUID> {

  Optional<PipelineRun> findByEventSourceIdAndExternalRunIdAndAttempt(
      UUID eventSourceId, String externalRunId, int attempt);

  Optional<PipelineRun> findByIdAndOrganisationId(UUID id, UUID organisationId);

  List<PipelineRun> findAllByProjectIdAndOrganisationIdOrderByUpdatedAtDesc(
      UUID projectId, UUID organisationId);
}
