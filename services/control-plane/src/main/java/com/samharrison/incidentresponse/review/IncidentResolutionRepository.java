package com.samharrison.incidentresponse.review;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentResolutionRepository extends JpaRepository<IncidentResolution, UUID> {
  Optional<IncidentResolution> findByIdAndOrganisationIdAndProjectId(
      UUID id, UUID organisationId, UUID projectId);
}
