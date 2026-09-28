package io.github.eliangilsierra.taskflow.tasks.api;

import io.github.eliangilsierra.taskflow.tasks.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull TaskStatus status) {}
