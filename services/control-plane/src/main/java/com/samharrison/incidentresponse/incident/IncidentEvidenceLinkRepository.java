package com.samharrison.incidentresponse.incident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEvidenceLinkRepository extends JpaRepository<IncidentEvidenceLink, UUID> {

  Optional<IncidentEvidenceLink> findByIncidentIdAndEvidenceId(UUID incidentId, UUID evidenceId);

  List<IncidentEvidenceLink>
      findAllByIncidentIdAndOrganisationIdAndProjectIdOrderByLinkedAtAscIdAsc(
          UUID incidentId, UUID organisationId, UUID projectId);

  List<IncidentEvidenceLink>
      findAllByEvidenceIdAndOrganisationIdAndProjectIdOrderByLinkedAtAscIdAsc(
          UUID evidenceId, UUID organisationId, UUID projectId);
}
