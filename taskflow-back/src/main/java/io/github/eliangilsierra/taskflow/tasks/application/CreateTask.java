package io.github.eliangilsierra.taskflow.tasks.application;

import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateTask {

  private final TaskRepository tasks;
  private final Clock clock;

  CreateTask(TaskRepository tasks, Clock clock) {
    this.tasks = tasks;
    this.clock = clock;
  }

  @Transactional
  public Task execute(long ownerId, Task.Details details) {
    return tasks.save(Task.create(ownerId, details, clock.instant()));
  }
}
