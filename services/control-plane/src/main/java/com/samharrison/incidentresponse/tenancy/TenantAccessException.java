package com.samharrison.incidentresponse.tenancy;

import org.springframework.http.HttpStatus;

public class TenantAccessException extends RuntimeException {

  private final String code;
  private final HttpStatus status;

  public TenantAccessException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
