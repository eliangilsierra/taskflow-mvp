package io.github.eliangilsierra.taskflow.auth.domain;

/** Port for one-way password hashing. */
public interface PasswordHasher {

  String hash(String rawPassword);

  boolean matches(String rawPassword, String passwordHash);
}
