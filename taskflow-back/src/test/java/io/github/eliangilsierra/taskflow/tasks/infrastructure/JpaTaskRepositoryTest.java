package io.github.eliangilsierra.taskflow.tasks.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.eliangilsierra.taskflow.auth.domain.User;
import io.github.eliangilsierra.taskflow.auth.domain.UserRepository;
import io.github.eliangilsierra.taskflow.shared.pagination.PageQuery;
import io.github.eliangilsierra.taskflow.shared.pagination.PageResult;
import io.github.eliangilsierra.taskflow.tasks.domain.Priority;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskCriteria;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskSort;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JpaTaskRepositoryTest {

  private static final Instant NOW = Instant.parse("2026-03-01T09:00:00Z");
  private static final PageQuery FIRST_PAGE = new PageQuery(0, 20);

  @Autowired TaskRepository tasks;
  @Autowired UserRepository users;

  private long ownerId;

  @BeforeEach
  void createOwner() {
    ownerId =
        users
            .save(
                User.register("owner-" + UUID.randomUUID() + "@example.com", "hash", "Owner", NOW))
            .id();
  }

  private Task save(String title, Priority priority, LocalDate dueDate, Instant createdAt) {
    return tasks.save(
        Task.create(ownerId, new Task.Details(title, null, priority, dueDate, null), createdAt));
  }

  private List<String> titles(TaskCriteria criteria) {
    return tasks.search(criteria, FIRST_PAGE).items().stream().map(Task::title).toList();
  }

  private TaskCriteria criteria(TaskSort sort) {
    return new TaskCriteria(ownerId, null, null, null, sort);
  }

  @Test
  void roundTripsATask() {
    Task saved = save("Write report", Priority.HIGH, LocalDate.of(2026, 3, 5), NOW);

    Task found = tasks.findByIdAndOwnerId(saved.id(), ownerId).orElseThrow();

    assertThat(found).isEqualTo(saved);
    assertThat(found.id()).isNotNull();
  }

  @Test
  void doesNotExposeTasksOfOtherOwners() {
    Task saved = save("Private", Priority.LOW, null, NOW);

    assertThat(tasks.findByIdAndOwnerId(saved.id(), ownerId + 1)).isEmpty();
    assertThat(
            tasks.search(new TaskCriteria(ownerId + 1, null, null, null, null), FIRST_PAGE).items())
        .isEmpty();
  }

  @Test
  void ordersByNewestFirstByDefault() {
    save("old", Priority.LOW, null, NOW);
    save("new", Priority.LOW, null, NOW.plusSeconds(60));

    assertThat(titles(criteria(TaskSort.NEWEST))).containsExactly("new", "old");
  }

  @Test
  void ordersByDueDateWithUndatedTasksLast() {
    save("undated", Priority.LOW, null, NOW);
    save("later", Priority.LOW, LocalDate.of(2026, 4, 1), NOW);
    save("sooner", Priority.LOW, LocalDate.of(2026, 3, 2), NOW);

    assertThat(titles(criteria(TaskSort.DUE_DATE))).containsExactly("sooner", "later", "undated");
  }

  @Test
  void ordersByPriorityFromHighToLow() {
    save("low", Priority.LOW, null, NOW);
    save("high", Priority.HIGH, null, NOW);
    save("medium", Priority.MEDIUM, null, NOW);

    assertThat(titles(criteria(TaskSort.PRIORITY))).containsExactly("high", "medium", "low");
  }

  @Test
  void filtersByStatusPriorityAndTitleText() {
    Task done = save("Pay rent", Priority.HIGH, null, NOW);
    tasks.save(done.changeStatus(TaskStatus.DONE, NOW));
    save("Pay taxes", Priority.LOW, null, NOW);
    save("Book flight", Priority.HIGH, null, NOW);

    assertThat(titles(new TaskCriteria(ownerId, TaskStatus.DONE, null, null, null)))
        .containsExactly("Pay rent");
    assertThat(titles(new TaskCriteria(ownerId, null, Priority.HIGH, null, TaskSort.PRIORITY)))
        .containsExactlyInAnyOrder("Pay rent", "Book flight");
    assertThat(titles(new TaskCriteria(ownerId, null, null, "PAY", null)))
        .containsExactlyInAnyOrder("Pay rent", "Pay taxes");
  }

  @Test
  void treatsLikeWildcardsInSearchTextLiterally() {
    save("100% done", Priority.LOW, null, NOW);
    save("something else", Priority.LOW, null, NOW);

    assertThat(titles(new TaskCriteria(ownerId, null, null, "%", null)))
        .containsExactly("100% done");
  }

  @Test
  void paginatesResults() {
    for (int i = 0; i < 5; i++) {
      save("task-" + i, Priority.LOW, null, NOW.plusSeconds(i));
    }

    PageResult<Task> page = tasks.search(criteria(TaskSort.NEWEST), new PageQuery(1, 2));

    assertThat(page.items()).extracting(Task::title).containsExactly("task-2", "task-1");
    assertThat(page.totalItems()).isEqualTo(5);
    assertThat(page.totalPages()).isEqualTo(3);
  }

  @Test
  void deletesATask() {
    Task saved = save("temp", Priority.LOW, null, NOW);

    tasks.deleteById(saved.id());

    assertThat(tasks.findByIdAndOwnerId(saved.id(), ownerId)).isEmpty();
  }

  @Test
  void findsOnlyDueUnsentOpenReminders() {
    Instant now = NOW.plusSeconds(3600);
    Task due = saveWithReminder("due", NOW);
    saveWithReminder("future", now.plusSeconds(60));
    tasks.save(saveWithReminder("sent", NOW).markReminderSent(now));
    tasks.save(saveWithReminder("done", NOW).changeStatus(TaskStatus.DONE, now));

    assertThat(tasks.findDueReminders(now, 10)).extracting(Task::id).containsExactly(due.id());
  }

  private Task saveWithReminder(String title, Instant reminderAt) {
    return tasks.save(
        Task.create(ownerId, new Task.Details(title, null, Priority.LOW, null, reminderAt), NOW));
  }
}
