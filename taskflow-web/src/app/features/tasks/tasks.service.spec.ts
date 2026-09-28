import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TaskFilters, TaskInput } from './task.model';
import { TasksService } from './tasks.service';

describe('TasksService', () => {
  let service: TasksService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TasksService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only the filters that are set', () => {
    const filters: TaskFilters = {
      status: '',
      priority: 'HIGH',
      search: '  report ',
      sort: 'DUE_DATE',
    };

    service.list(filters, 2, 10).subscribe();

    const request = http.expectOne((r) => r.url === '/api/tasks');
    expect(request.request.params.get('priority')).toBe('HIGH');
    expect(request.request.params.get('q')).toBe('report');
    expect(request.request.params.get('sort')).toBe('DUE_DATE');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.has('status')).toBeFalse();
    request.flush({ items: [], page: 2, size: 10, totalItems: 0, totalPages: 0 });
  });

  it('maps each operation to the matching endpoint', () => {
    const input: TaskInput = {
      title: 'A',
      description: null,
      priority: 'LOW',
      dueDate: null,
      reminderAt: null,
    };

    service.create(input).subscribe();
    expect(http.expectOne('/api/tasks').request.method).toBe('POST');

    service.update(3, input).subscribe();
    expect(http.expectOne('/api/tasks/3').request.method).toBe('PUT');

    service.changeStatus(3, 'DONE').subscribe();
    const patch = http.expectOne('/api/tasks/3/status');
    expect(patch.request.method).toBe('PATCH');
    expect(patch.request.body).toEqual({ status: 'DONE' });

    service.delete(3).subscribe();
    expect(http.expectOne('/api/tasks/3').request.method).toBe('DELETE');
  });
});
