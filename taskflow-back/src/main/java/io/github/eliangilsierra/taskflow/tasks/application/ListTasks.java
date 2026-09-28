package io.github.eliangilsierra.taskflow.tasks.application;

import io.github.eliangilsierra.taskflow.shared.pagination.PageQuery;
import io.github.eliangilsierra.taskflow.shared.pagination.PageResult;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskCriteria;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListTasks {

  private final TaskRepository tasks;

  ListTasks(TaskRepository tasks) {
    this.tasks = tasks;
  }

  @Transactional(readOnly = true)
  public PageResult<Task> execute(TaskCriteria criteria, PageQuery page) {
    return tasks.search(criteria, page);
  }
}
