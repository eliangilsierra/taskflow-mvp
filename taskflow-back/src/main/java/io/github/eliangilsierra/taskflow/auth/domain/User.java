package io.github.eliangilsierra.taskflow.auth.domain;

import java.time.Instant;

/** An account that owns tasks. */
public record User(
    Long id, String email, String passwordHash, String displayName, Instant createdAt) {

  public static User register(
      String email, String passwordHash, String displayName, Instant createdAt) {
    return new User(null, email, passwordHash, displayName, createdAt);
  }
}
