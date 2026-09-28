package io.github.eliangilsierra.taskflow.shared.pagination;

import java.util.List;
import java.util.function.Function;

/** Framework-independent page of results. */
public record PageResult<T>(List<T> items, int page, int size, long totalItems) {

  public PageResult {
    items = List.copyOf(items);
  }

  public int totalPages() {
    return (int) Math.ceil((double) totalItems / size);
  }

  public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
    return new PageResult<>(items.stream().<R>map(mapper).toList(), page, size, totalItems);
  }
}
