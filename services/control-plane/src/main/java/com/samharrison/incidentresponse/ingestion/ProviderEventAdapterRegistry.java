package com.samharrison.incidentresponse.ingestion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class ProviderEventAdapterRegistry {

  private final Map<EventProvider, ProviderEventAdapter> adapters;

  ProviderEventAdapterRegistry(List<ProviderEventAdapter> adapters) {
    this.adapters =
        adapters.stream()
            .collect(
                Collectors.toUnmodifiableMap(ProviderEventAdapter::provider, Function.identity()));
  }

  Optional<ProviderEventAdapter> find(EventProvider provider) {
    return Optional.ofNullable(adapters.get(provider));
  }
}
