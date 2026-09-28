import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { signal } from '@angular/core';
import { authGuard, guestGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('route guards', () => {
  const authenticated = signal(false);
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/tasks/new' } as RouterStateSnapshot;

  beforeEach(() => {
    authenticated.set(false);
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: { isAuthenticated: authenticated } }],
    });
  });

  it('authGuard lets signed-in users through', () => {
    authenticated.set(true);
    expect(TestBed.runInInjectionContext(() => authGuard(route, state))).toBeTrue();
  });

  it('authGuard redirects anonymous users to login, remembering where they were going', () => {
    const result = TestBed.runInInjectionContext(() => authGuard(route, state)) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(result)).toBe('/login?returnUrl=%2Ftasks%2Fnew');
  });

  it('guestGuard redirects signed-in users to the task list', () => {
    authenticated.set(true);
    const result = TestBed.runInInjectionContext(() => guestGuard(route, state)) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(result)).toBe('/tasks');
  });

  it('guestGuard lets anonymous users through', () => {
    expect(TestBed.runInInjectionContext(() => guestGuard(route, state))).toBeTrue();
  });
});
