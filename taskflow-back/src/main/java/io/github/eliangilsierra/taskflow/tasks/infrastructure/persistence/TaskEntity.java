package io.github.eliangilsierra.taskflow.tasks.infrastructure.persistence;

import io.github.eliangilsierra.taskflow.tasks.domain.Priority;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tasks")
class TaskEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(name = "owner_id", nullable = false)
  long ownerId;

  @Column(nullable = false, length = 120)
  String title;

  @Column(length = 2000)
  String description;

  // Stored as plain VARCHAR so the schema stays portable and validates against the migrations.
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 10)
  Priority priority;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  TaskStatus status;

  @Column(name = "due_date")
  LocalDate dueDate;

  @Column(name = "reminder_at")
  Instant reminderAt;

  @Column(name = "reminder_sent", nullable = false)
  boolean reminderSent;

  @Column(name = "created_at", nullable = false)
  Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  Instant updatedAt;

  protected TaskEntity() {}
}
