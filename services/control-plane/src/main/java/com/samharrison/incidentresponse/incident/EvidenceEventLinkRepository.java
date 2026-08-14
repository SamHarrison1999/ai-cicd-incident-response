package com.samharrison.incidentresponse.incident;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceEventLinkRepository extends JpaRepository<EvidenceEventLink, UUID> {

  Optional<EvidenceEventLink> findByEvidenceIdAndEventId(UUID evidenceId, UUID eventId);
}
