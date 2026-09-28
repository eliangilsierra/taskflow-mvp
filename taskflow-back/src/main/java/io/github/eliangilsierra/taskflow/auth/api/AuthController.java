package io.github.eliangilsierra.taskflow.auth.api;

import io.github.eliangilsierra.taskflow.auth.application.AuthenticateUser;
import io.github.eliangilsierra.taskflow.auth.application.GetCurrentUser;
import io.github.eliangilsierra.taskflow.auth.application.RegisterUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
class AuthController {

  private final RegisterUser registerUser;
  private final AuthenticateUser authenticateUser;
  private final GetCurrentUser getCurrentUser;

  AuthController(
      RegisterUser registerUser, AuthenticateUser authenticateUser, GetCurrentUser getCurrentUser) {
    this.registerUser = registerUser;
    this.authenticateUser = authenticateUser;
    this.getCurrentUser = getCurrentUser;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create an account and sign in")
  AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return AuthResponse.from(
        registerUser.execute(
            new RegisterUser.Command(request.email(), request.password(), request.displayName())));
  }

  @PostMapping("/login")
  @Operation(summary = "Exchange credentials for an access token")
  AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return AuthResponse.from(
        authenticateUser.execute(
            new AuthenticateUser.Command(request.email(), request.password())));
  }

  @GetMapping("/me")
  @Operation(summary = "Return the authenticated user")
  UserResponse me(@AuthenticationPrincipal Jwt jwt) {
    return UserResponse.from(getCurrentUser.execute(Long.parseLong(jwt.getSubject())));
  }
}
