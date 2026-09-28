package io.github.eliangilsierra.taskflow.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/** Typed, validated application settings bound from the {@code taskflow.*} namespace. */
@Validated
@ConfigurationProperties(prefix = "taskflow")
public record TaskflowProperties(@Valid @NotNull Jwt jwt) {

  /**
   * JWT settings.
   *
   * @param secret HMAC signing secret, at least 32 characters, supplied through the environment
   * @param ttl lifetime of an issued access token
   */
  public record Jwt(
      @NotBlank @Size(min = 32, message = "must be at least 32 characters") String secret,
      @NotNull @DefaultValue("1h") Duration ttl) {}
}
