import { HttpErrorResponse } from '@angular/common/http';
import { describeApiError } from './api-error';

describe('describeApiError', () => {
  it('uses the problem detail from the server', () => {
    const error = new HttpErrorResponse({ status: 409, error: { detail: 'Email taken.' } });
    expect(describeApiError(error)).toBe('Email taken.');
  });

  it('lists field violations', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { errors: [{ field: 'title', message: 'must not be blank' }] },
    });
    expect(describeApiError(error)).toBe('title: must not be blank');
  });

  it('explains network failures', () => {
    expect(describeApiError(new HttpErrorResponse({ status: 0 }))).toContain('Unable to reach');
  });

  it('falls back to a generic message for anything else', () => {
    expect(describeApiError(new Error('boom'))).toBe('Something went wrong. Please try again.');
  });
});
