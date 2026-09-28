package io.github.eliangilsierra.taskflow.tasks.infrastructure.reminders;

import io.github.eliangilsierra.taskflow.tasks.application.DispatchDueReminders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Polls for due reminders. Disable with {@code taskflow.reminders.enabled=false}. It assumes a
 * single application instance; see docs/decisions.md before scaling out.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(
    name = "taskflow.reminders.enabled",
    havingValue = "true",
    matchIfMissing = true)
class ReminderScheduler {

  private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

  private final DispatchDueReminders dispatchDueReminders;

  ReminderScheduler(DispatchDueReminders dispatchDueReminders) {
    this.dispatchDueReminders = dispatchDueReminders;
  }

  @Scheduled(fixedDelayString = "${taskflow.reminders.interval:PT1M}")
  void run() {
    int delivered = dispatchDueReminders.execute();
    if (delivered > 0) {
      log.info("Delivered {} reminder(s)", delivered);
    }
  }
}
