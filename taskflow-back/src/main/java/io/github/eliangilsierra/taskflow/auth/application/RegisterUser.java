package io.github.eliangilsierra.taskflow.auth.application;

import io.github.eliangilsierra.taskflow.auth.domain.AccessTokenIssuer;
import io.github.eliangilsierra.taskflow.auth.domain.EmailAlreadyRegisteredException;
import io.github.eliangilsierra.taskflow.auth.domain.PasswordHasher;
import io.github.eliangilsierra.taskflow.auth.domain.User;
import io.github.eliangilsierra.taskflow.auth.domain.UserRepository;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates a new account and signs the user in. */
@Service
public class RegisterUser {

  private static final Logger log = LoggerFactory.getLogger(RegisterUser.class);

  private final UserRepository users;
  private final PasswordHasher passwordHasher;
  private final AccessTokenIssuer tokenIssuer;
  private final Clock clock;

  RegisterUser(
      UserRepository users,
      PasswordHasher passwordHasher,
      AccessTokenIssuer tokenIssuer,
      Clock clock) {
    this.users = users;
    this.passwordHasher = passwordHasher;
    this.tokenIssuer = tokenIssuer;
    this.clock = clock;
  }

  @Transactional
  public AuthSession execute(Command command) {
    String email = EmailNormalizer.normalize(command.email());
    if (users.existsByEmail(email)) {
      throw new EmailAlreadyRegisteredException();
    }
    User user =
        users.save(
            User.register(
                email,
                passwordHasher.hash(command.password()),
                command.displayName().trim(),
                clock.instant()));
    log.info("Registered user id={}", user.id());
    return new AuthSession(user, tokenIssuer.issue(user));
  }

  public record Command(String email, String password, String displayName) {}
}
