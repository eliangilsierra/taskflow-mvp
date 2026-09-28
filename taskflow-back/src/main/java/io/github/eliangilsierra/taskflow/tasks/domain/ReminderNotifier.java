package io.github.eliangilsierra.taskflow.tasks.domain;

/** Port for delivering a reminder to the owner of a task. */
public interface ReminderNotifier {

  void notify(Task task);
}
