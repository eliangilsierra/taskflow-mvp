package io.github.eliangilsierra.taskflow.tasks.domain;

import io.github.eliangilsierra.taskflow.shared.error.NotFoundException;

/** Also used for tasks owned by someone else, so their existence is never revealed. */
public class TaskNotFoundException extends NotFoundException {

  public TaskNotFoundException(long taskId) {
    super("Task " + taskId + " was not found.");
  }
}
