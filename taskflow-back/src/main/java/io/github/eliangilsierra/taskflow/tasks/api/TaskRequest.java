package io.github.eliangilsierra.taskflow.tasks.api;

import io.github.eliangilsierra.taskflow.tasks.domain.Priority;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;

public record TaskRequest(
    @NotBlank @Size(max = Task.TITLE_MAX_LENGTH) String title,
    @Size(max = Task.DESCRIPTION_MAX_LENGTH) String description,
    @NotNull Priority priority,
    LocalDate dueDate,
    Instant reminderAt) {

  Task.Details toDetails() {
    return new Task.Details(title, description, priority, dueDate, reminderAt);
  }
}
