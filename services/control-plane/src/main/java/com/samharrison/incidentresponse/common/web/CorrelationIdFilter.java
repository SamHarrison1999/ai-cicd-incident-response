package com.samharrison.incidentresponse.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String HEADER_NAME = "X-Correlation-ID";
  public static final String MDC_KEY = "correlationId";

  private static final int MAX_LENGTH = 128;
  private static final Pattern ALLOWED_VALUE = Pattern.compile("[A-Za-z0-9._:-]+");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String correlationId = resolveCorrelationId(request.getHeader(HEADER_NAME));

    MDC.put(MDC_KEY, correlationId);
    response.setHeader(HEADER_NAME, correlationId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  String resolveCorrelationId(String suppliedValue) {
    if (suppliedValue == null || suppliedValue.isBlank()) {
      return UUID.randomUUID().toString();
    }

    String candidate = suppliedValue.trim();
    if (candidate.length() > MAX_LENGTH || !ALLOWED_VALUE.matcher(candidate).matches()) {
      return UUID.randomUUID().toString();
    }

    return candidate;
  }
}
