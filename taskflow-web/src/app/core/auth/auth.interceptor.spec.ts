import { HttpClient } from '@angular/common/http';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let logout: jasmine.Spy;
  let token: string | null;

  beforeEach(() => {
    token = 'jwt-token';
    logout = jasmine.createSpy('logout');
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: {
            get accessToken() {
              return token;
            },
            logout,
          },
        },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  it('adds the bearer token to API calls', () => {
    http.get('/api/tasks').subscribe();
    const request = controller.expectOne('/api/tasks');
    expect(request.request.headers.get('Authorization')).toBe('Bearer jwt-token');
    request.flush({});
  });

  it('does not attach the token to login or to other origins', () => {
    http.post('/api/auth/login', {}).subscribe();
    expect(
      controller.expectOne('/api/auth/login').request.headers.has('Authorization'),
    ).toBeFalse();

    http.get('https://example.com/data').subscribe();
    expect(
      controller.expectOne('https://example.com/data').request.headers.has('Authorization'),
    ).toBeFalse();
  });

  it('skips the header when signed out', () => {
    token = null;
    http.get('/api/tasks').subscribe();
    const request = controller.expectOne('/api/tasks');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush({});
  });

  it('ends the session when the API answers 401', () => {
    http.get('/api/tasks').subscribe({ error: () => undefined });
    controller.expectOne('/api/tasks').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(logout).toHaveBeenCalled();
  });

  it('keeps the session on a failed login attempt', () => {
    http.post('/api/auth/login', {}).subscribe({ error: () => undefined });
    controller.expectOne('/api/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(logout).not.toHaveBeenCalled();
  });
});
