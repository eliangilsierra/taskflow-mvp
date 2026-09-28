import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let login: jasmine.Spy;
  let router: Router;

  beforeEach(async () => {
    login = jasmine.createSpy('login');
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: { login } }],
    }).compileComponents();
    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
  });

  function fill(email: string, password: string): void {
    const root = fixture.nativeElement as HTMLElement;
    const [emailInput, passwordInput] = Array.from(root.querySelectorAll('input'));
    emailInput.value = email;
    emailInput.dispatchEvent(new Event('input'));
    passwordInput.value = password;
    passwordInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  function submit(): void {
    (fixture.nativeElement as HTMLElement).querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('does not call the API while the form is invalid', () => {
    fill('not-an-email', '');
    submit();
    expect(login).not.toHaveBeenCalled();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('valid email');
  });

  it('signs in and goes to the task list', () => {
    login.and.returnValue(of({}));
    fill('ana@example.com', 'secret');
    submit();
    expect(login).toHaveBeenCalledWith({ email: 'ana@example.com', password: 'secret' });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/tasks');
  });

  it('shows the server error and re-enables the form', () => {
    login.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401, error: { detail: 'Invalid email or password.' } })),
    );
    fill('ana@example.com', 'wrong');
    submit();
    const root = fixture.nativeElement as HTMLElement;
    expect(root.querySelector('[role="alert"]')?.textContent).toContain('Invalid email or password.');
    expect(root.querySelector<HTMLButtonElement>('button[type="submit"]')?.disabled).toBeFalse();
  });
});
