package io.github.eliangilsierra.taskflow.tasks.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A unit of work owned by one user. Instances are immutable: every change returns a new task.
 *
 * @param reminderAt moment at which the owner should be reminded, or {@code null}
 * @param reminderSent whether the reminder for the current {@code reminderAt} was already sent
 */
public record Task(
    Long id,
    long ownerId,
    String title,
    String description,
    Priority priority,
    TaskStatus status,
    LocalDate dueDate,
    Instant reminderAt,
    boolean reminderSent,
    Instant createdAt,
    Instant updatedAt) {

  public static final int TITLE_MAX_LENGTH = 120;
  public static final int DESCRIPTION_MAX_LENGTH = 2000;

  public Task {
    Objects.requireNonNull(priority, "priority");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(createdAt, "createdAt");
    Objects.requireNonNull(updatedAt, "updatedAt");
    title = title == null ? "" : title.trim();
    if (title.isEmpty()) {
      throw new IllegalArgumentException("Title must not be blank");
    }
    if (title.length() > TITLE_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Title must be at most " + TITLE_MAX_LENGTH + " characters");
    }
    description = description == null || description.isBlank() ? null : description.trim();
    if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Description must be at most " + DESCRIPTION_MAX_LENGTH + " characters");
    }
  }

  /** Editable attributes shared by creation and update. */
  public record Details(
      String title, String description, Priority priority, LocalDate dueDate, Instant reminderAt) {}

  public static Task create(long ownerId, Details details, Instant now) {
    return new Task(
        null,
        ownerId,
        details.title(),
        details.description(),
        details.priority(),
        TaskStatus.TODO,
        details.dueDate(),
        details.reminderAt(),
        false,
        now,
        now);
  }

  /** Replaces the editable attributes. Changing the reminder time re-arms the reminder. */
  public Task update(Details details, Instant now) {
    boolean reminderChanged = !Objects.equals(reminderAt, details.reminderAt());
    return new Task(
        id,
        ownerId,
        details.title(),
        details.description(),
        details.priority(),
        status,
        details.dueDate(),
        details.reminderAt(),
        reminderChanged ? false : reminderSent,
        createdAt,
        now);
  }

  public Task changeStatus(TaskStatus newStatus, Instant now) {
    return new Task(
        id,
        ownerId,
        title,
        description,
        priority,
        newStatus,
        dueDate,
        reminderAt,
        reminderSent,
        createdAt,
        now);
  }

  public Task markReminderSent(Instant now) {
    return new Task(
        id,
        ownerId,
        title,
        description,
        priority,
        status,
        dueDate,
        reminderAt,
        true,
        createdAt,
        now);
  }

  public boolean isOverdue(LocalDate today) {
    return status != TaskStatus.DONE && dueDate != null && dueDate.isBefore(today);
  }
}
