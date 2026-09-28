package io.github.eliangilsierra.taskflow.tasks.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.eliangilsierra.taskflow.tasks.domain.Priority;
import io.github.eliangilsierra.taskflow.tasks.domain.ReminderNotifier;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DispatchDueRemindersTest {

  private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");

  @Mock TaskRepository tasks;
  @Mock ReminderNotifier notifier;

  private DispatchDueReminders dispatch;

  @BeforeEach
  void setUp() {
    dispatch = new DispatchDueReminders(tasks, notifier, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private static Task task(long id) {
    Instant due = NOW.minusSeconds(60);
    return new Task(
        id, 1L, "Task " + id, null, Priority.LOW, TaskStatus.TODO, null, due, false, due, due);
  }

  @Test
  void notifiesAndMarksEachDueTaskAsSent() {
    when(tasks.findDueReminders(NOW, DispatchDueReminders.BATCH_SIZE))
        .thenReturn(List.of(task(1), task(2)));

    int delivered = dispatch.execute();

    assertThat(delivered).isEqualTo(2);
    ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
    verify(tasks, org.mockito.Mockito.times(2)).save(saved.capture());
    assertThat(saved.getAllValues()).allMatch(Task::reminderSent);
  }

  @Test
  void keepsAFailedReminderPendingAndContinuesWithTheRest() {
    Task failing = task(1);
    Task healthy = task(2);
    when(tasks.findDueReminders(eq(NOW), any(Integer.class))).thenReturn(List.of(failing, healthy));
    doThrow(new IllegalStateException("smtp down")).when(notifier).notify(failing);

    int delivered = dispatch.execute();

    assertThat(delivered).isEqualTo(1);
    verify(tasks, never()).save(failing.markReminderSent(NOW));
    verify(tasks).save(healthy.markReminderSent(NOW));
  }

  @Test
  void doesNothingWhenNoReminderIsDue() {
    when(tasks.findDueReminders(eq(NOW), any(Integer.class))).thenReturn(List.of());

    assertThat(dispatch.execute()).isZero();
    verify(notifier, never()).notify(any());
  }
}
