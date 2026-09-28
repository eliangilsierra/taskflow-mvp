package io.github.eliangilsierra.taskflow.auth.infrastructure.security;

import io.github.eliangilsierra.taskflow.auth.domain.AccessToken;
import io.github.eliangilsierra.taskflow.auth.domain.AccessTokenIssuer;
import io.github.eliangilsierra.taskflow.auth.domain.User;
import io.github.eliangilsierra.taskflow.shared.config.TaskflowProperties;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Issues HS256-signed JWTs whose subject is the user id. */
@Component
class JwtAccessTokenIssuer implements AccessTokenIssuer {

  static final String ISSUER = "taskflow";

  private final JwtEncoder encoder;
  private final TaskflowProperties.Jwt settings;
  private final Clock clock;

  JwtAccessTokenIssuer(JwtEncoder encoder, TaskflowProperties properties, Clock clock) {
    this.encoder = encoder;
    this.settings = properties.jwt();
    this.clock = clock;
  }

  @Override
  public AccessToken issue(User user) {
    Instant issuedAt = clock.instant();
    Instant expiresAt = issuedAt.plus(settings.ttl());
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(ISSUER)
            .subject(String.valueOf(user.id()))
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new AccessToken(value, expiresAt);
  }
}
