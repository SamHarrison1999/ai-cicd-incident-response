package com.samharrison.incidentresponse.ingestion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NormalisedCiEventRepository extends JpaRepository<NormalisedCiEvent, UUID> {

  Optional<NormalisedCiEvent> findByWebhookDeliveryId(UUID webhookDeliveryId);

  Optional<NormalisedCiEvent> findByIdAndOrganisationId(UUID id, UUID organisationId);

  List<NormalisedCiEvent> findAllByProjectIdAndOrganisationIdOrderByOccurredAtAsc(
      UUID projectId, UUID organisationId);

  List<NormalisedCiEvent> findAllByProjectIdAndOrganisationIdOrderByOccurredAtAscReceivedAtAscIdAsc(
      UUID projectId, UUID organisationId);
}
