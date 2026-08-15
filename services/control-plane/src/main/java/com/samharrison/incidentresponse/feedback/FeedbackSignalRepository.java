package com.samharrison.incidentresponse.feedback;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackSignalRepository extends JpaRepository<FeedbackSignal, UUID> {
  List<FeedbackSignal> findAllByOrganisationIdAndProjectIdOrderByCreatedAtAscIdAsc(
      UUID organisationId, UUID projectId);
}
