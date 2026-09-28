export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type TaskSort = 'NEWEST' | 'DUE_DATE' | 'PRIORITY';

export const PRIORITIES: readonly Priority[] = ['LOW', 'MEDIUM', 'HIGH'];
export const STATUSES: readonly TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

export const STATUS_LABELS: Record<TaskStatus, string> = {
  TODO: 'To do',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
};

export const PRIORITY_LABELS: Record<Priority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
};

export interface Task {
  id: number;
  title: string;
  description: string | null;
  priority: Priority;
  status: TaskStatus;
  dueDate: string | null;
  reminderAt: string | null;
  reminderSent: boolean;
  overdue: boolean;
  createdAt: string;
  updatedAt: string;
}

/** Editable fields, as sent when creating or replacing a task. */
export interface TaskInput {
  title: string;
  description: string | null;
  priority: Priority;
  dueDate: string | null;
  reminderAt: string | null;
}

export interface TaskFilters {
  status: TaskStatus | '';
  priority: Priority | '';
  search: string;
  sort: TaskSort;
}

export interface Page<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}
