package io.github.eliangilsierra.taskflow.auth.domain;

import java.time.Instant;

/** An issued bearer token and the moment it stops being valid. */
public record AccessToken(String value, Instant expiresAt) {}
