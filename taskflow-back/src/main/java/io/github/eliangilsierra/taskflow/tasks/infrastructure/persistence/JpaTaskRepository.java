package io.github.eliangilsierra.taskflow.tasks.infrastructure.persistence;

import io.github.eliangilsierra.taskflow.shared.pagination.PageQuery;
import io.github.eliangilsierra.taskflow.shared.pagination.PageResult;
import io.github.eliangilsierra.taskflow.tasks.domain.Priority;
import io.github.eliangilsierra.taskflow.tasks.domain.Task;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskCriteria;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskRepository;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskSort;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

/** Adapter that implements the {@link TaskRepository} port with Spring Data JPA. */
@Repository
class JpaTaskRepository implements TaskRepository {

  private static final char LIKE_ESCAPE = '\\';

  private final SpringDataTaskRepository delegate;

  JpaTaskRepository(SpringDataTaskRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public Task save(Task task) {
    return toDomain(delegate.save(toEntity(task)));
  }

  @Override
  public Optional<Task> findByIdAndOwnerId(long id, long ownerId) {
    return delegate.findByIdAndOwnerId(id, ownerId).map(JpaTaskRepository::toDomain);
  }

  @Override
  public PageResult<Task> search(TaskCriteria criteria, PageQuery page) {
    PageRequest request = PageRequest.of(page.page(), page.size());
    Page<TaskEntity> result = delegate.findAll(toSpecification(criteria), request);
    return new PageResult<>(
        result.getContent().stream().map(JpaTaskRepository::toDomain).toList(),
        page.page(),
        page.size(),
        result.getTotalElements());
  }

  @Override
  public void deleteById(long id) {
    delegate.deleteById(id);
  }

  @Override
  public List<Task> findDueReminders(Instant now, int limit) {
    return delegate.findDueReminders(now, PageRequest.of(0, limit)).stream()
        .map(JpaTaskRepository::toDomain)
        .toList();
  }

  private static Specification<TaskEntity> toSpecification(TaskCriteria criteria) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(builder.equal(root.get("ownerId"), criteria.ownerId()));
      if (criteria.status() != null) {
        predicates.add(builder.equal(root.get("status"), criteria.status()));
      }
      if (criteria.priority() != null) {
        predicates.add(builder.equal(root.get("priority"), criteria.priority()));
      }
      if (criteria.search() != null) {
        predicates.add(
            builder.like(
                builder.lower(root.get("title")),
                "%" + escapeLike(criteria.search().toLowerCase(Locale.ROOT)) + "%",
                LIKE_ESCAPE));
      }
      // The count query must not be ordered.
      if (!Long.class.equals(query.getResultType())) {
        query.orderBy(orderFor(criteria.sort(), root, builder));
      }
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }

  private static List<Order> orderFor(
      TaskSort sort, Root<TaskEntity> root, CriteriaBuilder builder) {
    List<Order> orders = new ArrayList<>();
    switch (sort) {
      case DUE_DATE -> {
        orders.add(
            builder.asc(
                builder
                    .<Integer>selectCase()
                    .when(builder.isNull(root.get("dueDate")), 1)
                    .otherwise(0)));
        orders.add(builder.asc(root.get("dueDate")));
      }
      case PRIORITY ->
          orders.add(
              builder.desc(
                  builder
                      .<Integer>selectCase()
                      .when(builder.equal(root.get("priority"), Priority.HIGH), 3)
                      .when(builder.equal(root.get("priority"), Priority.MEDIUM), 2)
                      .otherwise(1)));
      case NEWEST -> {}
    }
    orders.add(builder.desc(root.get("createdAt")));
    orders.add(builder.desc(root.get("id")));
    return orders;
  }

  private static String escapeLike(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  private static Task toDomain(TaskEntity entity) {
    return new Task(
        entity.id,
        entity.ownerId,
        entity.title,
        entity.description,
        entity.priority,
        entity.status,
        entity.dueDate,
        entity.reminderAt,
        entity.reminderSent,
        entity.createdAt,
        entity.updatedAt);
  }

  private static TaskEntity toEntity(Task task) {
    TaskEntity entity = new TaskEntity();
    entity.id = task.id();
    entity.ownerId = task.ownerId();
    entity.title = task.title();
    entity.description = task.description();
    entity.priority = task.priority();
    entity.status = task.status();
    entity.dueDate = task.dueDate();
    entity.reminderAt = task.reminderAt();
    entity.reminderSent = task.reminderSent();
    entity.createdAt = task.createdAt();
    entity.updatedAt = task.updatedAt();
    return entity;
  }
}
