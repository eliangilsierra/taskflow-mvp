package io.github.eliangilsierra.taskflow.shared.error;

/** Raised when the caller could not be authenticated. */
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }
}
