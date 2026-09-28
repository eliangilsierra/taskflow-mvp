package io.github.eliangilsierra.taskflow.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.eliangilsierra.taskflow.auth.domain.AccessToken;
import io.github.eliangilsierra.taskflow.auth.domain.AccessTokenIssuer;
import io.github.eliangilsierra.taskflow.auth.domain.EmailAlreadyRegisteredException;
import io.github.eliangilsierra.taskflow.auth.domain.PasswordHasher;
import io.github.eliangilsierra.taskflow.auth.domain.User;
import io.github.eliangilsierra.taskflow.auth.domain.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserTest {

  private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

  @Mock UserRepository users;
  @Mock PasswordHasher passwordHasher;
  @Mock AccessTokenIssuer tokenIssuer;

  private RegisterUser registerUser;

  @BeforeEach
  void setUp() {
    registerUser =
        new RegisterUser(users, passwordHasher, tokenIssuer, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void storesNormalizedEmailAndHashedPassword() {
    when(users.existsByEmail("ana@example.com")).thenReturn(false);
    when(passwordHasher.hash("s3cret-pass")).thenReturn("hashed");
    when(users.save(any(User.class))).thenAnswer(call -> withId(call.getArgument(0), 7L));
    when(tokenIssuer.issue(any(User.class)))
        .thenReturn(new AccessToken("token", NOW.plusSeconds(3600)));

    AuthSession session =
        registerUser.execute(
            new RegisterUser.Command("  Ana@Example.COM ", "s3cret-pass", " Ana "));

    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(users).save(saved.capture());
    assertThat(saved.getValue().email()).isEqualTo("ana@example.com");
    assertThat(saved.getValue().passwordHash()).isEqualTo("hashed");
    assertThat(saved.getValue().displayName()).isEqualTo("Ana");
    assertThat(saved.getValue().createdAt()).isEqualTo(NOW);
    assertThat(session.user().id()).isEqualTo(7L);
    assertThat(session.token().value()).isEqualTo("token");
  }

  @Test
  void rejectsAnEmailThatIsAlreadyRegistered() {
    when(users.existsByEmail("ana@example.com")).thenReturn(true);

    assertThatThrownBy(
            () -> registerUser.execute(new RegisterUser.Command("ana@example.com", "pw", "Ana")))
        .isInstanceOf(EmailAlreadyRegisteredException.class);
    verify(users, never()).save(any());
  }

  private static User withId(User user, long id) {
    return new User(id, user.email(), user.passwordHash(), user.displayName(), user.createdAt());
  }
}
