package com.samharrison.incidentresponse.authentication;

public class DuplicateEmailException extends RuntimeException {

  public DuplicateEmailException() {
    super("An account already exists for that email address.");
  }
}
