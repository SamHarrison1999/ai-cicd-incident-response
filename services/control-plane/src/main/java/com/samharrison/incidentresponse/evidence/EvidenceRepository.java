package com.samharrison.incidentresponse.evidence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {
  List<Evidence> findAllByOrganisationIdAndProjectIdOrderByOccurredAtDescIdDesc(
      UUID organisationId, UUID projectId);

  Optional<Evidence> findByIdAndOrganisationIdAndProjectId(
      UUID id, UUID organisationId, UUID projectId);
}
