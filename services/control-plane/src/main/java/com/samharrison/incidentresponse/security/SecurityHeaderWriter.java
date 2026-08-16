package com.samharrison.incidentresponse.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.header.HeaderWriter;

/** Writes the browser and transport headers required by the Phase 13 boundary. */
public final class SecurityHeaderWriter implements HeaderWriter {

  static final String CONTENT_SECURITY_POLICY =
      "default-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'";
  static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=(), payment=()";
  static final String REFERRER_POLICY = "no-referrer";
  static final String HSTS = "max-age=31536000; includeSubDomains";

  @Override
  public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
    setIfAbsent(response, "Content-Security-Policy", CONTENT_SECURITY_POLICY);
    setIfAbsent(response, "X-Content-Type-Options", "nosniff");
    setIfAbsent(response, "X-Frame-Options", "DENY");
    setIfAbsent(response, "Referrer-Policy", REFERRER_POLICY);
    setIfAbsent(response, "Permissions-Policy", PERMISSIONS_POLICY);
    if (request.isSecure()) {
      setIfAbsent(response, "Strict-Transport-Security", HSTS);
    }
  }

  private static void setIfAbsent(
      HttpServletResponse response, String headerName, String headerValue) {
    if (response.getHeader(headerName) == null) {
      response.setHeader(headerName, headerValue);
    }
  }
}
