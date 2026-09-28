package io.github.eliangilsierra.taskflow.auth.domain;

import io.github.eliangilsierra.taskflow.shared.error.UnauthorizedException;

/** Deliberately generic so callers cannot tell whether the email or the password was wrong. */
public class InvalidCredentialsException extends UnauthorizedException {

  public InvalidCredentialsException() {
    super("Invalid email or password.");
  }
}
