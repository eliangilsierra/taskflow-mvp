import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/http/api.config';
import { Page, Task, TaskFilters, TaskInput, TaskStatus } from './task.model';

const TASKS_URL = `${API_URL}/tasks`;

@Injectable({ providedIn: 'root' })
export class TasksService {
  private readonly http = inject(HttpClient);

  list(filters: TaskFilters, page: number, size: number): Observable<Page<Task>> {
    let params = new HttpParams()
      .set('sort', filters.sort)
      .set('page', page)
      .set('size', size);
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.priority) {
      params = params.set('priority', filters.priority);
    }
    if (filters.search.trim()) {
      params = params.set('q', filters.search.trim());
    }
    return this.http.get<Page<Task>>(TASKS_URL, { params });
  }

  get(id: number): Observable<Task> {
    return this.http.get<Task>(`${TASKS_URL}/${id}`);
  }

  create(input: TaskInput): Observable<Task> {
    return this.http.post<Task>(TASKS_URL, input);
  }

  update(id: number, input: TaskInput): Observable<Task> {
    return this.http.put<Task>(`${TASKS_URL}/${id}`, input);
  }

  changeStatus(id: number, status: TaskStatus): Observable<Task> {
    return this.http.patch<Task>(`${TASKS_URL}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${TASKS_URL}/${id}`);
  }
}
