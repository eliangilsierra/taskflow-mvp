import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { Task } from './task.model';
import { TaskFormComponent } from './task-form.component';
import { TasksService } from './tasks.service';

const EXISTING: Task = {
  id: 7,
  title: 'Existing',
  description: 'Notes',
  priority: 'LOW',
  status: 'TODO',
  dueDate: '2030-01-01',
  reminderAt: null,
  reminderSent: false,
  overdue: false,
  createdAt: '2026-03-01T09:00:00Z',
  updatedAt: '2026-03-01T09:00:00Z',
};

describe('TaskFormComponent', () => {
  let fixture: ComponentFixture<TaskFormComponent>;
  let service: jasmine.SpyObj<TasksService>;
  let router: Router;
  const root = (): HTMLElement => fixture.nativeElement as HTMLElement;

  function setup(id?: string): void {
    service = jasmine.createSpyObj<TasksService>('TasksService', ['get', 'create', 'update']);
    service.get.and.returnValue(of(EXISTING));
    service.create.and.returnValue(of(EXISTING));
    service.update.and.returnValue(of(EXISTING));
    TestBed.configureTestingModule({
      imports: [TaskFormComponent],
      providers: [provideRouter([]), { provide: TasksService, useValue: service }],
    });
    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
    fixture = TestBed.createComponent(TaskFormComponent);
    if (id !== undefined) {
      fixture.componentRef.setInput('id', id);
    }
    fixture.detectChanges();
  }

  function type(selector: string, value: string): void {
    const element = root().querySelector<HTMLInputElement>(selector)!;
    element.value = value;
    element.dispatchEvent(new Event('input'));
  }

  function submit(): void {
    root().querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('blocks submission while the title is blank', () => {
    setup();
    type('input[formControlName="title"]', '   ');
    submit();

    expect(service.create).not.toHaveBeenCalled();
    expect(root().textContent).toContain('A title is required');
  });

  it('creates a task with trimmed text and empty optionals as null', () => {
    setup();
    type('input[formControlName="title"]', '  Ship it  ');
    submit();

    expect(service.create).toHaveBeenCalledWith({
      title: 'Ship it',
      description: null,
      priority: 'MEDIUM',
      dueDate: null,
      reminderAt: null,
    });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/tasks');
  });

  it('loads and updates an existing task when an id is provided', () => {
    setup('7');

    expect(service.get).toHaveBeenCalledWith(7);
    expect(root().querySelector<HTMLInputElement>('input[formControlName="title"]')!.value).toBe('Existing');

    type('input[formControlName="title"]', 'Renamed');
    submit();

    expect(service.update).toHaveBeenCalledWith(7, jasmine.objectContaining({ title: 'Renamed' }));
    expect(service.create).not.toHaveBeenCalled();
  });
});
