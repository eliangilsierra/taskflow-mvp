package io.github.eliangilsierra.taskflow.tasks.domain;

/**
 * Filters for listing the tasks of one owner.
 *
 * @param status only tasks in this status, or {@code null} for any
 * @param priority only tasks with this priority, or {@code null} for any
 * @param search case-insensitive text the title must contain, or {@code null} for any
 */
public record TaskCriteria(
    long ownerId, TaskStatus status, Priority priority, String search, TaskSort sort) {

  public TaskCriteria {
    search = search == null || search.isBlank() ? null : search.trim();
    sort = sort == null ? TaskSort.NEWEST : sort;
  }
}
