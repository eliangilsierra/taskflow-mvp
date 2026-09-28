package io.github.eliangilsierra.taskflow.auth.application;

import io.github.eliangilsierra.taskflow.auth.domain.User;
import io.github.eliangilsierra.taskflow.auth.domain.UserRepository;
import io.github.eliangilsierra.taskflow.shared.error.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Loads the account behind a valid token. */
@Service
public class GetCurrentUser {

  private final UserRepository users;

  GetCurrentUser(UserRepository users) {
    this.users = users;
  }

  @Transactional(readOnly = true)
  public User execute(long userId) {
    return users
        .findById(userId)
        .orElseThrow(() -> new UnauthorizedException("The session is no longer valid."));
  }
}
