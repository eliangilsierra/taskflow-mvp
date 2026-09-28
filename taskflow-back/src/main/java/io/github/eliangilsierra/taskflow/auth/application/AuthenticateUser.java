package io.github.eliangilsierra.taskflow.auth.application;

import io.github.eliangilsierra.taskflow.auth.domain.AccessTokenIssuer;
import io.github.eliangilsierra.taskflow.auth.domain.InvalidCredentialsException;
import io.github.eliangilsierra.taskflow.auth.domain.PasswordHasher;
import io.github.eliangilsierra.taskflow.auth.domain.User;
import io.github.eliangilsierra.taskflow.auth.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Verifies credentials and issues an access token. */
@Service
public class AuthenticateUser {

  private final UserRepository users;
  private final PasswordHasher passwordHasher;
  private final AccessTokenIssuer tokenIssuer;

  AuthenticateUser(
      UserRepository users, PasswordHasher passwordHasher, AccessTokenIssuer tokenIssuer) {
    this.users = users;
    this.passwordHasher = passwordHasher;
    this.tokenIssuer = tokenIssuer;
  }

  @Transactional(readOnly = true)
  public AuthSession execute(Command command) {
    User user =
        users
            .findByEmail(EmailNormalizer.normalize(command.email()))
            .filter(
                candidate -> passwordHasher.matches(command.password(), candidate.passwordHash()))
            .orElseThrow(InvalidCredentialsException::new);
    return new AuthSession(user, tokenIssuer.issue(user));
  }

  public record Command(String email, String password) {}
}
