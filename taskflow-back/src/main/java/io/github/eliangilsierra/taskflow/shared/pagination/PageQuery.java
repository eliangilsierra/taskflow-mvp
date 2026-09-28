package io.github.eliangilsierra.taskflow.shared.pagination;

/** Framework-independent pagination request. Pages are zero-based. */
public record PageQuery(int page, int size) {

  public static final int MAX_SIZE = 100;

  public PageQuery {
    if (page < 0) {
      throw new IllegalArgumentException("Page index must not be negative");
    }
    if (size < 1 || size > MAX_SIZE) {
      throw new IllegalArgumentException("Page size must be between 1 and " + MAX_SIZE);
    }
  }
}
