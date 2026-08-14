package com.samharrison.incidentresponse.incident;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEventLinkRepository extends JpaRepository<IncidentEventLink, UUID> {

  Optional<IncidentEventLink> findByEventId(UUID eventId);
}
