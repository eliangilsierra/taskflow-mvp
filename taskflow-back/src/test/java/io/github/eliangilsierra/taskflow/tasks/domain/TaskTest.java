package io.github.eliangilsierra.taskflow.tasks.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TaskTest {

  private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");
  private static final Instant LATER = NOW.plusSeconds(600);

  private static Task.Details details(String title, Instant reminderAt) {
    return new Task.Details(
        title, "  notes  ", Priority.HIGH, LocalDate.of(2026, 3, 5), reminderAt);
  }

  @Test
  void createsATodoTaskWithNormalizedText() {
    Task task = Task.create(1L, details("  Write report ", null), NOW);

    assertThat(task.title()).isEqualTo("Write report");
    assertThat(task.description()).isEqualTo("notes");
    assertThat(task.status()).isEqualTo(TaskStatus.TODO);
    assertThat(task.reminderSent()).isFalse();
    assertThat(task.createdAt()).isEqualTo(NOW).isEqualTo(task.updatedAt());
  }

  @Test
  void treatsABlankDescriptionAsAbsent() {
    Task task = Task.create(1L, new Task.Details("t", "   ", Priority.LOW, null, null), NOW);

    assertThat(task.description()).isNull();
  }

  @Test
  void rejectsBlankOrTooLongTitles() {
    assertThatThrownBy(() -> Task.create(1L, details("   ", null), NOW))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Task.create(1L, details("x".repeat(121), null), NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateKeepsIdentityStatusAndCreationTime() {
    Task original =
        Task.create(1L, details("A", null), NOW).changeStatus(TaskStatus.IN_PROGRESS, NOW);

    Task updated = original.update(details("B", null), LATER);

    assertThat(updated.title()).isEqualTo("B");
    assertThat(updated.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    assertThat(updated.createdAt()).isEqualTo(NOW);
    assertThat(updated.updatedAt()).isEqualTo(LATER);
  }

  @Test
  void changingTheReminderTimeRearmsIt() {
    Task sent = Task.create(1L, details("A", NOW), NOW).markReminderSent(NOW);

    assertThat(sent.update(details("A", NOW), LATER).reminderSent()).isTrue();
    assertThat(sent.update(details("A", LATER), LATER).reminderSent()).isFalse();
  }

  @Test
  void isOverdueOnlyWhenPastDueAndNotDone() {
    Task task = Task.create(1L, details("A", null), NOW);
    LocalDate dueDate = task.dueDate();

    assertThat(task.isOverdue(dueDate)).isFalse();
    assertThat(task.isOverdue(dueDate.plusDays(1))).isTrue();
    assertThat(task.changeStatus(TaskStatus.DONE, NOW).isOverdue(dueDate.plusDays(1))).isFalse();
  }
}
