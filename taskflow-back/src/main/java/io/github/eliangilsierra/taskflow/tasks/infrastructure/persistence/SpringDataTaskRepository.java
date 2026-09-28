package io.github.eliangilsierra.taskflow.tasks.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataTaskRepository
    extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {

  Optional<TaskEntity> findByIdAndOwnerId(long id, long ownerId);

  @Query(
      """
      select t from TaskEntity t
      where t.reminderSent = false
        and t.reminderAt is not null
        and t.reminderAt <= :now
        and t.status <> io.github.eliangilsierra.taskflow.tasks.domain.TaskStatus.DONE
      order by t.reminderAt asc
      """)
  List<TaskEntity> findDueReminders(@Param("now") Instant now, Pageable limit);
}
