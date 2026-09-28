package io.github.eliangilsierra.taskflow.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Size(max = 254) String email, @NotBlank @Size(max = 72) String password) {}
