package io.github.eliangilsierra.taskflow.auth.domain;

import io.github.eliangilsierra.taskflow.shared.error.ConflictException;

public class EmailAlreadyRegisteredException extends ConflictException {

  public EmailAlreadyRegisteredException() {
    super("An account with this email already exists.");
  }
}
