package com.samharrison.incidentresponse.feedback;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackAggregateRepository extends JpaRepository<FeedbackAggregate, UUID> {
  List<FeedbackAggregate> findAllByOrganisationIdAndProjectIdOrderByWindowEndDescIdDesc(
      UUID organisationId, UUID projectId);
}
