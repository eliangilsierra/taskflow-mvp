package io.github.eliangilsierra.taskflow.auth.domain;

import java.util.Optional;

/** Port for persisting and querying users. */
public interface UserRepository {

  Optional<User> findById(long id);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  /**
   * Persists a new user.
   *
   * @throws EmailAlreadyRegisteredException if the email is already taken
   */
  User save(User user);
}
