package com.samharrison.incidentresponse.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityHeaderWriterTest {

  private final SecurityHeaderWriter writer = new SecurityHeaderWriter();

  @Test
  void writesBaselineBrowserHeadersForHttpRequests() {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();

    writer.writeHeaders(request, response);

    assertThat(response.getHeader("Content-Security-Policy"))
        .isEqualTo(SecurityHeaderWriter.CONTENT_SECURITY_POLICY);
    assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
    assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
    assertThat(response.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
    assertThat(response.getHeader("Permissions-Policy"))
        .isEqualTo(SecurityHeaderWriter.PERMISSIONS_POLICY);
    assertThat(response.getHeader("Strict-Transport-Security")).isNull();
  }

  @Test
  void writesHstsOnlyForSecureRequests() {
    var request = new MockHttpServletRequest();
    request.setSecure(true);
    var response = new MockHttpServletResponse();

    writer.writeHeaders(request, response);

    assertThat(response.getHeader("Strict-Transport-Security"))
        .isEqualTo(SecurityHeaderWriter.HSTS);
  }

  @Test
  void preservesHeadersWrittenByAnEarlierFilter() {
    var request = new MockHttpServletRequest();
    request.setSecure(true);
    var response = new MockHttpServletResponse();
    response.setHeader("X-Frame-Options", "SAMEORIGIN");
    response.setHeader("Strict-Transport-Security", "max-age=60");

    writer.writeHeaders(request, response);

    assertThat(response.getHeader("X-Frame-Options")).isEqualTo("SAMEORIGIN");
    assertThat(response.getHeader("Strict-Transport-Security")).isEqualTo("max-age=60");
  }
}
