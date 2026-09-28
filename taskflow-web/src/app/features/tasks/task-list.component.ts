import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, debounceTime } from 'rxjs';
import { describeApiError } from '../../core/http/api-error';
import {
  PRIORITIES,
  PRIORITY_LABELS,
  Page,
  STATUSES,
  STATUS_LABELS,
  Task,
  TaskFilters,
  TaskStatus,
} from './task.model';
import { TasksService } from './tasks.service';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.css',
})
export class TaskListComponent implements OnInit {
  private readonly tasksService = inject(TasksService);
  private readonly destroyRef = inject(DestroyRef);
  private loadSubscription?: Subscription;

  protected readonly priorities = PRIORITIES;
  protected readonly statuses = STATUSES;
  protected readonly priorityLabels = PRIORITY_LABELS;
  protected readonly statusLabels = STATUS_LABELS;

  protected readonly filters = inject(NonNullableFormBuilder).group({
    search: [''],
    status: ['' as TaskFilters['status']],
    priority: ['' as TaskFilters['priority']],
    sort: ['DUE_DATE' as TaskFilters['sort']],
  });

  protected readonly page = signal<Page<Task> | null>(null);
  protected readonly pageIndex = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.filters.valueChanges
      .pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.goToPage(0));
    this.load();
  }

  protected goToPage(index: number): void {
    this.pageIndex.set(index);
    this.load();
  }

  protected toggleDone(task: Task): void {
    const next: TaskStatus = task.status === 'DONE' ? 'TODO' : 'DONE';
    this.tasksService.changeStatus(task.id, next).subscribe({
      next: () => this.load(),
      error: (error: unknown) => this.error.set(describeApiError(error)),
    });
  }

  protected remove(task: Task): void {
    if (!confirm(`Delete "${task.title}"?`)) {
      return;
    }
    this.tasksService.delete(task.id).subscribe({
      next: () => this.afterDelete(),
      error: (error: unknown) => this.error.set(describeApiError(error)),
    });
  }

  private afterDelete(): void {
    const current = this.page();
    const wasLastItemOnPage = current !== null && current.items.length === 1;
    this.goToPage(
      wasLastItemOnPage && this.pageIndex() > 0 ? this.pageIndex() - 1 : this.pageIndex(),
    );
  }

  private load(): void {
    this.loadSubscription?.unsubscribe();
    this.loading.set(true);
    this.error.set(null);
    this.loadSubscription = this.tasksService
      .list(this.filters.getRawValue(), this.pageIndex(), PAGE_SIZE)
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: (error: unknown) => {
          this.error.set(describeApiError(error));
          this.loading.set(false);
        },
      });
  }
}
