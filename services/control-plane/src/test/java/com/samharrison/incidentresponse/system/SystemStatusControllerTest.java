package com.samharrison.incidentresponse.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class SystemStatusControllerTest {

  @Test
  void returnsDeterministicServiceStatus() {
    Instant now = Instant.parse("2026-08-01T12:00:00Z");
    SystemStatusController controller =
        new SystemStatusController(Clock.fixed(now, ZoneOffset.UTC), null);

    var response = controller.status();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .isEqualTo(new SystemStatusResponse("control-plane", "development", "UP", now));
  }
}
