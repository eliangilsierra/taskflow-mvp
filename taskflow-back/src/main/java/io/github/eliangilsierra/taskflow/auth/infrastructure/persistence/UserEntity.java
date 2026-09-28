package io.github.eliangilsierra.taskflow.auth.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false, unique = true, length = 254)
  String email;

  @Column(name = "password_hash", nullable = false, length = 100)
  String passwordHash;

  @Column(name = "display_name", nullable = false, length = 80)
  String displayName;

  @Column(name = "created_at", nullable = false)
  Instant createdAt;

  protected UserEntity() {}
}
