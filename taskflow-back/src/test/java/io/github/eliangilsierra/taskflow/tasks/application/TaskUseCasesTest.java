package io.github.eliangilsierra.taskflow.tasks.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.eliangilsierra.taskflow.tasks.domain.Priority;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskNotFoundException;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskUseCasesTest {

  private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");
  private static final Instant EARLIER = NOW.minusSeconds(3600);
  private static final Task.Details DETAILS =
      new Task.Details("Write report", null, Priority.MEDIUM, null, null);

  @Mock TaskRepository tasks;

  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
  private Task existing;

  @BeforeEach
  void setUp() {
    existing =
        new Task(
            5L,
            1L,
            "Old",
            null,
            Priority.LOW,
            TaskStatus.TODO,
            null,
            null,
            false,
            EARLIER,
            EARLIER);
  }

  @Test
  void createStampsTheCurrentTime() {
    when(tasks.save(any(Task.class))).thenAnswer(call -> call.getArgument(0));

    Task created = new CreateTask(tasks, clock).execute(1L, DETAILS);

    assertThat(created.ownerId()).isEqualTo(1L);
    assertThat(created.createdAt()).isEqualTo(NOW);
  }

  @Test
  void updateAppliesDetailsToTheOwnedTask() {
    when(tasks.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.of(existing));
    when(tasks.save(any(Task.class))).thenAnswer(call -> call.getArgument(0));

    Task updated = new UpdateTask(tasks, clock).execute(1L, 5L, DETAILS);

    assertThat(updated.title()).isEqualTo("Write report");
    assertThat(updated.updatedAt()).isEqualTo(NOW);
  }

  @Test
  void changeStatusPersistsTheNewStatus() {
    when(tasks.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.of(existing));
    when(tasks.save(any(Task.class))).thenAnswer(call -> call.getArgument(0));

    Task changed = new ChangeTaskStatus(tasks, clock).execute(1L, 5L, TaskStatus.DONE);

    assertThat(changed.status()).isEqualTo(TaskStatus.DONE);
  }

  @Test
  void operationsOnSomeoneElsesTaskLookLikeMissingTasks() {
    when(tasks.findByIdAndOwnerId(5L, 2L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> new GetTask(tasks).execute(2L, 5L))
        .isInstanceOf(TaskNotFoundException.class);
    assertThatThrownBy(() -> new UpdateTask(tasks, clock).execute(2L, 5L, DETAILS))
        .isInstanceOf(TaskNotFoundException.class);
    assertThatThrownBy(() -> new ChangeTaskStatus(tasks, clock).execute(2L, 5L, TaskStatus.DONE))
        .isInstanceOf(TaskNotFoundException.class);
    assertThatThrownBy(() -> new DeleteTask(tasks).execute(2L, 5L))
        .isInstanceOf(TaskNotFoundException.class);
    verify(tasks, never()).deleteById(5L);
    verify(tasks, never()).save(any());
  }

  @Test
  void deleteRemovesTheOwnedTask() {
    when(tasks.findByIdAndOwnerId(5L, 1L)).thenReturn(Optional.of(existing));

    new DeleteTask(tasks).execute(1L, 5L);

    verify(tasks).deleteById(5L);
  }
}
