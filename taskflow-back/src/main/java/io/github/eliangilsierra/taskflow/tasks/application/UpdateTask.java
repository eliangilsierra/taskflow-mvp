package io.github.eliangilsierra.taskflow.tasks.application;

import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskNotFoundException;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateTask {

  private final TaskRepository tasks;
  private final Clock clock;

  UpdateTask(TaskRepository tasks, Clock clock) {
    this.tasks = tasks;
    this.clock = clock;
  }

  @Transactional
  public Task execute(long ownerId, long taskId, Task.Details details) {
    Task task =
        tasks
            .findByIdAndOwnerId(taskId, ownerId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));
    return tasks.save(task.update(details, clock.instant()));
  }
}
