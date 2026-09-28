import { HttpErrorResponse } from '@angular/common/http';

/** RFC 7807 problem details as returned by the API. */
interface ProblemDetails {
  title?: string;
  detail?: string;
  errors?: { field: string; message: string }[];
}

const FALLBACK_MESSAGE = 'Something went wrong. Please try again.';

/** Turns any HTTP failure into a message that is safe to show to the user. */
export function describeApiError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return FALLBACK_MESSAGE;
  }
  if (error.status === 0) {
    return 'Unable to reach the server. Check your connection and try again.';
  }
  const problem = error.error as ProblemDetails | null;
  if (problem?.errors?.length) {
    return problem.errors.map((e) => `${e.field}: ${e.message}`).join('. ');
  }
  return problem?.detail ?? FALLBACK_MESSAGE;
}
