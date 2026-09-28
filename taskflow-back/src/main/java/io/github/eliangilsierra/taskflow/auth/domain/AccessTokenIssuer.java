package io.github.eliangilsierra.taskflow.auth.domain;

/** Port for issuing access tokens for authenticated users. */
public interface AccessTokenIssuer {

  AccessToken issue(User user);
}
