package com.samharrison.incidentresponse.authentication;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
public class AuthenticationConfiguration {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  SecretKey authenticationSigningKey(AuthenticationProperties properties) {
    return new SecretKeySpec(
        properties.signingSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey authenticationSigningKey) {
    return NimbusJwtEncoder.withSecretKey(authenticationSigningKey)
        .algorithm(MacAlgorithm.HS256)
        .build();
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey authenticationSigningKey) {
    return NimbusJwtDecoder.withSecretKey(authenticationSigningKey)
        .macAlgorithm(MacAlgorithm.HS256)
        .build();
  }
}
