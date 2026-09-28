package io.github.eliangilsierra.taskflow.auth.api;

import io.github.eliangilsierra.taskflow.auth.domain.User;

public record UserResponse(long id, String email, String displayName) {

  static UserResponse from(User user) {
    return new UserResponse(user.id(), user.email(), user.displayName());
  }
}
