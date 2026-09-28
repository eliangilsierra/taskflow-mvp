package io.github.eliangilsierra.taskflow.shared.error;

/** Raised when an operation conflicts with the current state of a resource. */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
