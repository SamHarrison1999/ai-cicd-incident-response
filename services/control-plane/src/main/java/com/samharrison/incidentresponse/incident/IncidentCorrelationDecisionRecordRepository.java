package com.samharrison.incidentresponse.incident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentCorrelationDecisionRecordRepository
    extends JpaRepository<IncidentCorrelationDecisionRecord, UUID> {

  Optional<IncidentCorrelationDecisionRecord> findByEventId(UUID eventId);

  List<IncidentCorrelationDecisionRecord> findAllByOrganisationIdAndProjectIdOrderByCreatedAtDesc(
      UUID organisationId, UUID projectId);
}
