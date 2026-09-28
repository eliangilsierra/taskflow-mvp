package io.github.eliangilsierra.taskflow.shared.pagination;

import java.util.List;

/** JSON representation of a {@link PageResult}. */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

  public static <T> PageResponse<T> from(PageResult<T> result) {
    return new PageResponse<>(
        result.items(), result.page(), result.size(), result.totalItems(), result.totalPages());
  }
}
