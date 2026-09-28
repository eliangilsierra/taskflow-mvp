package io.github.eliangilsierra.taskflow.tasks.domain;

import io.github.eliangilsierra.taskflow.shared.pagination.PageQuery;
import io.github.eliangilsierra.taskflow.shared.pagination.PageResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Port for persisting and querying tasks. Every lookup is scoped to the owning user. */
public interface TaskRepository {

  Task save(Task task);

  Optional<Task> findByIdAndOwnerId(long id, long ownerId);

  PageResult<Task> search(TaskCriteria criteria, PageQuery page);

  void deleteById(long id);

  /** Tasks whose reminder is due, not yet sent and not completed, oldest reminder first. */
  List<Task> findDueReminders(Instant now, int limit);
}
