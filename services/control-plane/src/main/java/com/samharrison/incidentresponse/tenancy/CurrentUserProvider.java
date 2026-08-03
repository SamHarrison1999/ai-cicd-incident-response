package com.samharrison.incidentresponse.tenancy;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

  public UUID requireUserId(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof Jwt jwt)) {
      throw new TenantAccessException(
          org.springframework.http.HttpStatus.UNAUTHORIZED,
          "AUTHENTICATION_REQUIRED",
          "A valid access token is required.");
    }

    try {
      return UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      throw new TenantAccessException(
          org.springframework.http.HttpStatus.UNAUTHORIZED,
          "INVALID_ACCESS_TOKEN",
          "The access token subject is invalid.");
    }
  }
}
