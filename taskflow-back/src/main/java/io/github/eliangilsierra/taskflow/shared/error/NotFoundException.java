package io.github.eliangilsierra.taskflow.shared.error;

/** Raised when a requested resource does not exist or is not visible to the caller. */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}
