import { Routes } from '@angular/router';

export const TASK_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./task-list.component').then((m) => m.TaskListComponent),
  },
  {
    path: 'new',
    loadComponent: () => import('./task-form.component').then((m) => m.TaskFormComponent),
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./task-form.component').then((m) => m.TaskFormComponent),
  },
];
