package com.samharrison.incidentresponse.system;

import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemStatusController {

  private final Clock clock;
  private final BuildProperties buildProperties;

  public SystemStatusController(Clock clock, @Nullable BuildProperties buildProperties) {
    this.clock = clock;
    this.buildProperties = buildProperties;
  }

  @GetMapping("/status")
  public ResponseEntity<SystemStatusResponse> status() {
    String version = buildProperties == null ? "development" : buildProperties.getVersion();

    return ResponseEntity.ok(
        new SystemStatusResponse("control-plane", version, "UP", Instant.now(clock)));
  }
}
