package io.github.eliangilsierra.taskflow.shared.security;

import org.springframework.security.oauth2.jwt.Jwt;

/** Extracts the caller's identity from a validated access token. */
public final class AuthenticatedUser {

  private AuthenticatedUser() {}

  public static long idOf(Jwt jwt) {
    return Long.parseLong(jwt.getSubject());
  }
}
