package com.samharrison.incidentresponse.authentication;

import com.samharrison.incidentresponse.identity.UserAccount;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtAccessTokenService {

  private final JwtEncoder jwtEncoder;
  private final AuthenticationProperties properties;
  private final Clock clock;

  @Autowired
  public JwtAccessTokenService(JwtEncoder jwtEncoder, AuthenticationProperties properties) {
    this(jwtEncoder, properties, Clock.systemUTC());
  }

  JwtAccessTokenService(JwtEncoder jwtEncoder, AuthenticationProperties properties, Clock clock) {
    this.jwtEncoder = jwtEncoder;
    this.properties = properties;
    this.clock = clock;
  }

  public AccessToken issue(UserAccount user, UUID sessionId) {
    Instant issuedAt = clock.instant();
    Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .audience(List.of(properties.audience()))
            .subject(user.getId().toString())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .claim("sid", sessionId.toString())
            .claim("email", user.getNormalisedEmail())
            .build();

    String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

    return new AccessToken(token, properties.accessTokenTtl().toSeconds());
  }

  public record AccessToken(String value, long expiresInSeconds) {}
}
