package io.github.eliangilsierra.taskflow.tasks.application;

import io.github.eliangilsierra.taskflow.tasks.domain.ReminderNotifier;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sends the reminders that have come due. A failure to notify one task is logged and retried on the
 * next run, without blocking the other tasks.
 */
@Service
public class DispatchDueReminders {

  static final int BATCH_SIZE = 100;

  private static final Logger log = LoggerFactory.getLogger(DispatchDueReminders.class);

  private final TaskRepository tasks;
  private final ReminderNotifier notifier;
  private final Clock clock;

  DispatchDueReminders(TaskRepository tasks, ReminderNotifier notifier, Clock clock) {
    this.tasks = tasks;
    this.notifier = notifier;
    this.clock = clock;
  }

  /** Returns how many reminders were delivered. */
  public int execute() {
    Instant now = clock.instant();
    List<Task> due = tasks.findDueReminders(now, BATCH_SIZE);
    int delivered = 0;
    for (Task task : due) {
      try {
        notifier.notify(task);
        tasks.save(task.markReminderSent(now));
        delivered++;
      } catch (RuntimeException ex) {
        log.warn("Could not deliver reminder for task id={}; will retry", task.id(), ex);
      }
    }
    return delivered;
  }
}
