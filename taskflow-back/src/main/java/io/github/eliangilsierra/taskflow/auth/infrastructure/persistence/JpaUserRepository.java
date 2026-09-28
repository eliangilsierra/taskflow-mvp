package io.github.eliangilsierra.taskflow.auth.infrastructure.persistence;

import io.github.eliangilsierra.taskflow.auth.domain.EmailAlreadyRegisteredException;
import io.github.eliangilsierra.taskflow.auth.domain.User;
import io.github.eliangilsierra.taskflow.auth.domain.UserRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

/** Adapter that implements the {@link UserRepository} port with Spring Data JPA. */
@Repository
class JpaUserRepository implements UserRepository {

  private final SpringDataUserRepository delegate;

  JpaUserRepository(SpringDataUserRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public Optional<User> findById(long id) {
    return delegate.findById(id).map(JpaUserRepository::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return delegate.findByEmail(email).map(JpaUserRepository::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return delegate.existsByEmail(email);
  }

  @Override
  public User save(User user) {
    try {
      return toDomain(delegate.saveAndFlush(toEntity(user)));
    } catch (DataIntegrityViolationException ex) {
      // Covers the race between the existence check and the insert.
      throw new EmailAlreadyRegisteredException();
    }
  }

  private static User toDomain(UserEntity entity) {
    return new User(
        entity.id, entity.email, entity.passwordHash, entity.displayName, entity.createdAt);
  }

  private static UserEntity toEntity(User user) {
    UserEntity entity = new UserEntity();
    entity.id = user.id();
    entity.email = user.email();
    entity.passwordHash = user.passwordHash();
    entity.displayName = user.displayName();
    entity.createdAt = user.createdAt();
    return entity;
  }
}
