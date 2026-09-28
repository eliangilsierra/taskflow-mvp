package io.github.eliangilsierra.taskflow.tasks.api;

import io.github.eliangilsierra.taskflow.tasks.domain.Priority;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
    long id,
    String title,
    String description,
    Priority priority,
    TaskStatus status,
    LocalDate dueDate,
    Instant reminderAt,
    boolean reminderSent,
    boolean overdue,
    Instant createdAt,
    Instant updatedAt) {

  static TaskResponse from(Task task, LocalDate today) {
    return new TaskResponse(
        task.id(),
        task.title(),
        task.description(),
        task.priority(),
        task.status(),
        task.dueDate(),
        task.reminderAt(),
        task.reminderSent(),
        task.isOverdue(today),
        task.createdAt(),
        task.updatedAt());
  }
}
