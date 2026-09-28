package io.github.eliangilsierra.taskflow.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.eliangilsierra.taskflow.auth.domain.AccessToken;
import io.github.eliangilsierra.taskflow.auth.domain.AccessTokenIssuer;
import io.github.eliangilsierra.taskflow.auth.domain.InvalidCredentialsException;
import io.github.eliangilsierra.taskflow.auth.domain.PasswordHasher;
import io.github.eliangilsierra.taskflow.auth.domain.User;
import io.github.eliangilsierra.taskflow.auth.domain.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserTest {

  private static final User ANA =
      new User(1L, "ana@example.com", "hashed", "Ana", Instant.parse("2026-01-01T00:00:00Z"));

  @Mock UserRepository users;
  @Mock PasswordHasher passwordHasher;
  @Mock AccessTokenIssuer tokenIssuer;

  private AuthenticateUser authenticateUser;

  @BeforeEach
  void setUp() {
    authenticateUser = new AuthenticateUser(users, passwordHasher, tokenIssuer);
  }

  @Test
  void issuesATokenForValidCredentials() {
    when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(ANA));
    when(passwordHasher.matches("right", "hashed")).thenReturn(true);
    when(tokenIssuer.issue(ANA)).thenReturn(new AccessToken("token", Instant.MAX));

    AuthSession session =
        authenticateUser.execute(new AuthenticateUser.Command("ANA@example.com", "right"));

    assertThat(session.user()).isEqualTo(ANA);
    assertThat(session.token().value()).isEqualTo("token");
  }

  @Test
  void rejectsAWrongPassword() {
    when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(ANA));
    when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

    assertThatThrownBy(
            () ->
                authenticateUser.execute(new AuthenticateUser.Command("ana@example.com", "wrong")))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void rejectsAnUnknownEmailWithTheSameError() {
    when(users.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                authenticateUser.execute(new AuthenticateUser.Command("nobody@example.com", "pw")))
        .isInstanceOf(InvalidCredentialsException.class);
  }
}
