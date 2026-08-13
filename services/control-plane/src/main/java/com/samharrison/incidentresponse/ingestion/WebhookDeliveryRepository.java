package com.samharrison.incidentresponse.ingestion;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

  Optional<WebhookDelivery> findByEventSourceIdAndProviderDeliveryId(
      UUID eventSourceId, String providerDeliveryId);

  Optional<WebhookDelivery> findByIdAndOrganisationId(UUID id, UUID organisationId);
}
