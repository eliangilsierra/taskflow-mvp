package io.github.eliangilsierra.taskflow.tasks.application;

import io.github.eliangilsierra.taskflow.tasks.domain.TaskNotFoundException;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteTask {

  private final TaskRepository tasks;

  DeleteTask(TaskRepository tasks) {
    this.tasks = tasks;
  }

  @Transactional
  public void execute(long ownerId, long taskId) {
    tasks.findByIdAndOwnerId(taskId, ownerId).orElseThrow(() -> new TaskNotFoundException(taskId));
    tasks.deleteById(taskId);
  }
}
