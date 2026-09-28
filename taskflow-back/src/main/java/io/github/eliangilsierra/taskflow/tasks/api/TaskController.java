package io.github.eliangilsierra.taskflow.tasks.api;

import io.github.eliangilsierra.taskflow.shared.pagination.PageQuery;
import io.github.eliangilsierra.taskflow.shared.pagination.PageResponse;
import io.github.eliangilsierra.taskflow.shared.security.AuthenticatedUser;
import io.github.eliangilsierra.taskflow.tasks.application.ChangeTaskStatus;
import io.github.eliangilsierra.taskflow.tasks.application.CreateTask;
import io.github.eliangilsierra.taskflow.tasks.application.DeleteTask;
import io.github.eliangilsierra.taskflow.tasks.application.GetTask;
import io.github.eliangilsierra.taskflow.tasks.application.ListTasks;
import io.github.eliangilsierra.taskflow.tasks.application.UpdateTask;
import io.github.eliangilsierra.taskflow.tasks.domain.Priority;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskCriteria;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskSort;
import io.github.eliangilsierra.taskflow.tasks.domain.TaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks")
class TaskController {

  private final CreateTask createTask;
  private final UpdateTask updateTask;
  private final ChangeTaskStatus changeTaskStatus;
  private final GetTask getTask;
  private final ListTasks listTasks;
  private final DeleteTask deleteTask;
  private final Clock clock;

  TaskController(
      CreateTask createTask,
      UpdateTask updateTask,
      ChangeTaskStatus changeTaskStatus,
      GetTask getTask,
      ListTasks listTasks,
      DeleteTask deleteTask,
      Clock clock) {
    this.createTask = createTask;
    this.updateTask = updateTask;
    this.changeTaskStatus = changeTaskStatus;
    this.getTask = getTask;
    this.listTasks = listTasks;
    this.deleteTask = deleteTask;
    this.clock = clock;
  }

  @GetMapping
  @Operation(summary = "List the caller's tasks, filtered, sorted and paginated")
  PageResponse<TaskResponse> list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) Priority priority,
      @RequestParam(name = "q", required = false) String search,
      @RequestParam(defaultValue = "NEWEST") TaskSort sort,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var criteria = new TaskCriteria(AuthenticatedUser.idOf(jwt), status, priority, search, sort);
    LocalDate today = today();
    return PageResponse.from(
        listTasks
            .execute(criteria, new PageQuery(page, size))
            .map(t -> TaskResponse.from(t, today)));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get one task")
  TaskResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
    return TaskResponse.from(getTask.execute(AuthenticatedUser.idOf(jwt), id), today());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a task")
  TaskResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TaskRequest request) {
    return TaskResponse.from(
        createTask.execute(AuthenticatedUser.idOf(jwt), request.toDetails()), today());
  }

  @PutMapping("/{id}")
  @Operation(summary = "Replace the editable fields of a task")
  TaskResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable long id,
      @Valid @RequestBody TaskRequest request) {
    return TaskResponse.from(
        updateTask.execute(AuthenticatedUser.idOf(jwt), id, request.toDetails()), today());
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Change the status of a task")
  TaskResponse changeStatus(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable long id,
      @Valid @RequestBody StatusRequest request) {
    return TaskResponse.from(
        changeTaskStatus.execute(AuthenticatedUser.idOf(jwt), id, request.status()), today());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a task")
  void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
    deleteTask.execute(AuthenticatedUser.idOf(jwt), id);
  }

  private LocalDate today() {
    return LocalDate.now(clock);
  }
}
