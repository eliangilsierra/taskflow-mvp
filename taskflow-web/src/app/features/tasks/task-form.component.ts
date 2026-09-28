import { Component, OnInit, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { describeApiError } from '../../core/http/api-error';
import { fromDateTimeLocalValue, toDateTimeLocalValue } from '../../shared/date-time';
import { PRIORITIES, PRIORITY_LABELS, Priority, Task, TaskInput } from './task.model';
import { TasksService } from './tasks.service';

/** Creates a task, or edits one when routed with an `id` (bound from the URL). */
@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './task-form.component.html',
})
export class TaskFormComponent implements OnInit {
  private readonly tasksService = inject(TasksService);
  private readonly router = inject(Router);

  /** Bound from the `:id` route parameter; absent when creating. */
  readonly id = input<string>();

  protected readonly priorities = PRIORITIES;
  protected readonly priorityLabels = PRIORITY_LABELS;
  protected readonly form = inject(NonNullableFormBuilder).group({
    title: ['', [Validators.required, Validators.maxLength(120), Validators.pattern(/\S/)]],
    description: ['', [Validators.maxLength(2000)]],
    priority: ['MEDIUM' as Priority, [Validators.required]],
    dueDate: [''],
    reminderAt: [''],
  });
  protected readonly loading = signal(false);
  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);

  protected get isEditing(): boolean {
    return this.id() !== undefined;
  }

  ngOnInit(): void {
    const id = this.id();
    if (id === undefined) {
      return;
    }
    this.loading.set(true);
    this.tasksService.get(Number(id)).subscribe({
      next: (task) => {
        this.fill(task);
        this.loading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(describeApiError(error));
        this.loading.set(false);
      },
    });
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const input = this.toInput();
    const id = this.id();
    const request =
      id === undefined
        ? this.tasksService.create(input)
        : this.tasksService.update(Number(id), input);
    request.subscribe({
      next: () => void this.router.navigateByUrl('/tasks'),
      error: (error: unknown) => {
        this.error.set(describeApiError(error));
        this.submitting.set(false);
      },
    });
  }

  private fill(task: Task): void {
    this.form.setValue({
      title: task.title,
      description: task.description ?? '',
      priority: task.priority,
      dueDate: task.dueDate ?? '',
      reminderAt: toDateTimeLocalValue(task.reminderAt),
    });
  }

  private toInput(): TaskInput {
    const value = this.form.getRawValue();
    return {
      title: value.title.trim(),
      description: value.description.trim() || null,
      priority: value.priority,
      dueDate: value.dueDate || null,
      reminderAt: fromDateTimeLocalValue(value.reminderAt),
    };
  }
}
