import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthResponse } from './auth.models';
import { AuthService } from './auth.service';

const FUTURE = new Date(Date.now() + 3_600_000).toISOString();
const RESPONSE: AuthResponse = {
  accessToken: 'jwt-token',
  tokenType: 'Bearer',
  expiresAt: FUTURE,
  user: { id: 1, email: 'ana@example.com', displayName: 'Ana' },
};

describe('AuthService', () => {
  function setup(): { service: AuthService; http: HttpTestingController; router: Router } {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    return {
      service: TestBed.inject(AuthService),
      http: TestBed.inject(HttpTestingController),
      router,
    };
  }

  beforeEach(() => localStorage.clear());

  it('starts signed out', () => {
    const { service } = setup();
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.accessToken).toBeNull();
  });

  it('stores the session after a successful login', () => {
    const { service, http } = setup();

    service.login({ email: 'ana@example.com', password: 'secret' }).subscribe();
    const request = http.expectOne('/api/auth/login');
    expect(request.request.body).toEqual({ email: 'ana@example.com', password: 'secret' });
    request.flush(RESPONSE);

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.accessToken).toBe('jwt-token');
    expect(service.user()?.displayName).toBe('Ana');
    expect(localStorage.getItem('taskflow.session')).toContain('jwt-token');
  });

  it('stores the session after registering', () => {
    const { service, http } = setup();

    service
      .register({ email: 'ana@example.com', password: 'secret-pass', displayName: 'Ana' })
      .subscribe();
    http.expectOne('/api/auth/register').flush(RESPONSE);

    expect(service.isAuthenticated()).toBeTrue();
  });

  it('restores an unexpired session from storage', () => {
    localStorage.setItem(
      'taskflow.session',
      JSON.stringify({ accessToken: 'stored', expiresAt: FUTURE, user: RESPONSE.user }),
    );
    const { service } = setup();

    expect(service.accessToken).toBe('stored');
  });

  it('ignores an expired or corrupt stored session', () => {
    localStorage.setItem(
      'taskflow.session',
      JSON.stringify({
        accessToken: 'old',
        expiresAt: '2000-01-01T00:00:00Z',
        user: RESPONSE.user,
      }),
    );
    expect(setup().service.isAuthenticated()).toBeFalse();

    TestBed.resetTestingModule();
    localStorage.setItem('taskflow.session', '{not json');
    expect(setup().service.isAuthenticated()).toBeFalse();
  });

  it('clears the session on logout and returns to the login page', () => {
    const { service, http, router } = setup();
    service.login({ email: 'a@b.co', password: 'x' }).subscribe();
    http.expectOne('/api/auth/login').flush(RESPONSE);

    service.logout();

    expect(service.isAuthenticated()).toBeFalse();
    expect(localStorage.getItem('taskflow.session')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
