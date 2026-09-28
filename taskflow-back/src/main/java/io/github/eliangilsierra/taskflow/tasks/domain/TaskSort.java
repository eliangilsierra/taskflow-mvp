package io.github.eliangilsierra.taskflow.tasks.domain;

/** Supported orderings when listing tasks. */
public enum TaskSort {
  /** Most recently created first. */
  NEWEST,
  /** Earliest due date first; tasks without a due date last. */
  DUE_DATE,
  /** Highest priority first. */
  PRIORITY
}
