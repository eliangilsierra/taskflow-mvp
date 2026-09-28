package io.github.eliangilsierra.taskflow.auth.application;

import io.github.eliangilsierra.taskflow.auth.domain.AccessToken;
import io.github.eliangilsierra.taskflow.auth.domain.User;

/** The result of a successful registration or login. */
public record AuthSession(User user, AccessToken token) {}
