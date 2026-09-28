import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Page, Task } from './task.model';
import { TaskListComponent } from './task-list.component';
import { TasksService } from './tasks.service';

function task(overrides: Partial<Task> = {}): Task {
  return {
    id: 1,
    title: 'Write report',
    description: null,
    priority: 'HIGH',
    status: 'TODO',
    dueDate: null,
    reminderAt: null,
    reminderSent: false,
    overdue: false,
    createdAt: '2026-03-01T09:00:00Z',
    updatedAt: '2026-03-01T09:00:00Z',
    ...overrides,
  };
}

function pageOf(items: Task[], totalPages = 1, page = 0): Page<Task> {
  return { items, page, size: 10, totalItems: items.length, totalPages };
}

describe('TaskListComponent', () => {
  let fixture: ComponentFixture<TaskListComponent>;
  let service: jasmine.SpyObj<TasksService>;
  const root = (): HTMLElement => fixture.nativeElement as HTMLElement;

  function create(page: Page<Task>): void {
    service = jasmine.createSpyObj<TasksService>('TasksService', ['list', 'changeStatus', 'delete']);
    service.list.and.returnValue(of(page));
    TestBed.configureTestingModule({
      imports: [TaskListComponent],
      providers: [provideRouter([]), { provide: TasksService, useValue: service }],
    });
    fixture = TestBed.createComponent(TaskListComponent);
    fixture.detectChanges();
  }

  it('renders the loaded tasks with their badges', () => {
    create(pageOf([task({ dueDate: '2000-01-01', overdue: true }), task({ id: 2, title: 'Other', priority: 'LOW' })]));

    expect(root().querySelectorAll('.task').length).toBe(2);
    expect(root().textContent).toContain('Write report');
    expect(root().querySelector('.badge-overdue')?.textContent).toContain('Overdue');
    expect(service.list).toHaveBeenCalledWith(
      { search: '', status: '', priority: '', sort: 'DUE_DATE' },
      0,
      10,
    );
  });

  it('shows an empty state when nothing matches', () => {
    create(pageOf([]));
    expect(root().querySelector('.empty')?.textContent).toContain('No tasks match');
  });

  it('marks a task as done and reloads', () => {
    create(pageOf([task()]));
    service.changeStatus.and.returnValue(of(task({ status: 'DONE' })));

    root().querySelector<HTMLInputElement>('.task-check')!.click();

    expect(service.changeStatus).toHaveBeenCalledWith(1, 'DONE');
    expect(service.list).toHaveBeenCalledTimes(2);
  });

  it('deletes a task only after confirmation', () => {
    create(pageOf([task()]));
    service.delete.and.returnValue(of(undefined));
    const confirmSpy = spyOn(window, 'confirm');
    const deleteButton = root().querySelector<HTMLButtonElement>('.btn-danger')!;

    confirmSpy.and.returnValue(false);
    deleteButton.click();
    expect(service.delete).not.toHaveBeenCalled();

    confirmSpy.and.returnValue(true);
    deleteButton.click();
    expect(service.delete).toHaveBeenCalledWith(1);
  });

  it('paginates', () => {
    create(pageOf([task()], 3, 0));

    const next = Array.from(root().querySelectorAll<HTMLButtonElement>('.pager button')).find((b) =>
      b.textContent?.includes('Next'),
    )!;
    next.click();

    expect(service.list.calls.mostRecent().args[1]).toBe(1);
  });

  it('reloads from the first page when a filter changes, after a debounce', fakeAsync(() => {
    create(pageOf([task()], 3, 2));
    service.list.calls.reset();

    const select = root().querySelector<HTMLSelectElement>('select')!;
    select.value = 'DONE';
    select.dispatchEvent(new Event('change'));
    tick(300);

    expect(service.list).toHaveBeenCalledTimes(1);
    expect(service.list.calls.mostRecent().args[0].status).toBe('DONE');
    expect(service.list.calls.mostRecent().args[1]).toBe(0);
  }));
});
