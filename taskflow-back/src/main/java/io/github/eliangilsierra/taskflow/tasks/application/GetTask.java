package io.github.eliangilsierra.taskflow.tasks.application;

import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskNotFoundException;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetTask {

  private final TaskRepository tasks;

  GetTask(TaskRepository tasks) {
    this.tasks = tasks;
  }

  @Transactional(readOnly = true)
  public Task execute(long ownerId, long taskId) {
    return tasks
        .findByIdAndOwnerId(taskId, ownerId)
        .orElseThrow(() -> new TaskNotFoundException(taskId));
  }
}
