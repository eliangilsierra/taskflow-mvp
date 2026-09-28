package io.github.eliangilsierra.taskflow.tasks.infrastructure.reminders;

import io.github.eliangilsierra.taskflow.tasks.domain.ReminderNotifier;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default notifier that only writes to the log. Replace it with an email or push adapter by
 * providing another {@link ReminderNotifier} implementation.
 */
@Component
class LoggingReminderNotifier implements ReminderNotifier {

  private static final Logger log = LoggerFactory.getLogger(LoggingReminderNotifier.class);

  @Override
  public void notify(Task task) {
    log.info("Reminder due for task id={} owner={}", task.id(), task.ownerId());
  }
}
