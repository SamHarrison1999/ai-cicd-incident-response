package com.samharrison.incidentresponse.incident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

  Optional<Incident> findByIdAndOrganisationIdAndProjectId(
      UUID id, UUID organisationId, UUID projectId);

  List<Incident> findAllByOrganisationIdAndProjectIdOrderByDetectedAtDescIdDesc(
      UUID organisationId, UUID projectId);
}
