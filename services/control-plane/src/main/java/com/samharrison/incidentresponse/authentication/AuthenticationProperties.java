package com.samharrison.incidentresponse.authentication;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "application.authentication")
public record AuthenticationProperties(
    @NotBlank String issuer,
    @NotBlank String audience,
    @NotBlank @Size(min = 32) String signingSecret,
    Duration accessTokenTtl,
    Duration refreshTokenTtl,
    @NotBlank String refreshCookieName,
    boolean secureCookies,
    @Min(1) int passwordMinimumLength) {}
