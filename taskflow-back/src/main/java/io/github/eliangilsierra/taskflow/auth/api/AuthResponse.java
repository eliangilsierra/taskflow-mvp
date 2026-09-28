package io.github.eliangilsierra.taskflow.auth.api;

import io.github.eliangilsierra.taskflow.auth.application.AuthSession;
import java.time.Instant;

public record AuthResponse(
    String accessToken, String tokenType, Instant expiresAt, UserResponse user) {

  static AuthResponse from(AuthSession session) {
    return new AuthResponse(
        session.token().value(),
        "Bearer",
        session.token().expiresAt(),
        UserResponse.from(session.user()));
  }
}
