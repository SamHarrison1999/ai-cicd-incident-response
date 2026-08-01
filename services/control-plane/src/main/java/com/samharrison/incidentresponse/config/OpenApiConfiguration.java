package com.samharrison.incidentresponse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

  @Bean
  OpenAPI controlPlaneOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("AI-Assisted CI/CD Incident Response Control Plane API")
                .description(
                    "Authoritative workflow API for CI/CD events, incidents, recommendations, and audit history.")
                .version("v1")
                .contact(new Contact().name("Samuel Harrison")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
