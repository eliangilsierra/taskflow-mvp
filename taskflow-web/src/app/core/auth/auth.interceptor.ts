import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../http/api.config';
import { AuthService } from './auth.service';

const PUBLIC_PATHS = [`${API_URL}/auth/login`, `${API_URL}/auth/register`];

/** Adds the bearer token to API calls and ends the session when the server rejects it. */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const isApiCall = request.url.startsWith(API_URL);
  const isPublic = PUBLIC_PATHS.includes(request.url);
  const token = auth.accessToken;

  const outgoing =
    isApiCall && !isPublic && token
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;

  return next(outgoing).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && isApiCall && !isPublic) {
        auth.logout();
      }
      return throwError(() => error);
    }),
  );
};
