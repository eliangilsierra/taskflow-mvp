# Architecture decision records

Short records of the significant decisions, in the order they were made. Each one states the context,
the decision and its consequences, including the trade-offs that were accepted.

## ADR-001: Single repository (monorepo) with two projects

- **Context.** The API and the web app are developed and released together and share one contract.
- **Decision.** Keep `taskflow-back` and `taskflow-web` in one repository, with a shared
  `docker-compose.yml`, CI workflows and documentation.
- **Consequences.** One pull request can change both sides of the contract, and setup is a single
  clone. CI uses path filters so each side only builds when it changes.

## ADR-002: Modular monolith with Clean Architecture per feature

- **Context.** The problem is small; microservices would add operational cost with no benefit
  (YAGNI). But the code must stay easy to extend and test.
- **Decision.** One Spring Boot application organized by feature (`auth`, `tasks`). Inside each
  feature, layers `domain`, `application`, `infrastructure` and `api` with dependencies pointing
  inward, and persistence/security hidden behind ports.
- **Consequences.** Business rules are unit-testable without Spring or a database, and adapters can
  be replaced independently. The cost is some mapping code between JPA entities and domain records,
  accepted deliberately to keep the domain free of persistence concerns.

## ADR-003: Stateless JWT authentication

- **Context.** The SPA and API are separate; the API should scale horizontally without shared
  session storage.
- **Decision.** Issue short-lived (1 hour) HS256 JWTs signed with a secret from the environment,
  validated by Spring Security's resource server. Passwords are hashed with BCrypt. Login errors are
  identical for unknown email and wrong password.
- **Consequences.** No server-side session state. Tokens cannot be revoked before they expire, and
  there are no refresh tokens yet, so users sign in again after expiry.
- **Known trade-off.** The SPA keeps the token in `localStorage`, which is readable by any script
  running on the page. It is mitigated by Angular's built-in output sanitization, no use of
  `innerHTML`, and a strict Content-Security-Policy in the nginx image. An `HttpOnly` cookie with
  refresh-token rotation is the stronger design and is on the roadmap.
- **Not yet done.** Login rate limiting and account lockout.

## ADR-004: Flyway migrations with `ddl-auto=validate`

- **Context.** Letting Hibernate generate the schema hides drift and is unsafe in production.
- **Decision.** Flyway owns the schema; Hibernate only validates it. Enums are stored as `VARCHAR`
  (forced with `@JdbcTypeCode`) so the migrations stay portable and validate on MySQL.
- **Consequences.** Schema changes are explicit, reviewable and versioned. Entities and migrations
  must be edited together, and the application will not start if they disagree.

## ADR-005: No server-side rendering

- **Context.** The Angular scaffold enabled SSR and prerendering by default.
- **Decision.** Remove SSR and its Express server. The app is a client-rendered SPA served by nginx.
- **Consequences.** Fewer dependencies, a smaller attack surface and simpler builds. Every screen is
  behind authentication, so SEO does not benefit from SSR.

## ADR-006: Same-origin API, no CORS

- **Context.** CORS is easy to misconfigure and widens the attack surface.
- **Decision.** The browser only talks to one origin: `ng serve` proxies `/api` in development and
  nginx proxies it in Docker.
- **Consequences.** No CORS configuration in the backend. Hosting the API on a different origin in
  the future requires adding an explicit allow-list.

## ADR-007: Reminders by polling, on a single instance

- **Context.** Reminders must fire near their due time; a message broker would be excessive.
- **Decision.** A scheduled job runs every minute, delivers due reminders through a
  `ReminderNotifier` port and marks them as sent.
- **Consequences.** Simple and dependency-free, with at-least-once behavior per task (a failed
  delivery is retried). Running several backend instances would deliver duplicates; that would need
  a distributed lock (for example ShedLock) or a `SELECT … FOR UPDATE SKIP LOCKED` claim step.

## ADR-008: Tests on H2 in MySQL mode

- **Context.** Fast, dependency-free tests matter for contributors and for CI.
- **Decision.** Backend integration tests run the real Flyway migrations on H2 in MySQL
  compatibility mode. The application was also verified manually against MySQL 8.
- **Consequences.** Some MySQL-specific behavior (collations, engine details) is not exercised
  automatically. Adding Testcontainers-based tests against MySQL is planned.

## ADR-009: Opaque 404 for other users' tasks

- **Decision.** Every task query is scoped to the authenticated owner, and a task owned by someone
  else yields the same `404` as one that does not exist.
- **Consequences.** Task identifiers cannot be enumerated; authorization is enforced in a single
  place (the repository queries) instead of scattered checks.

## ADR-010: Automated formatting and linting

- **Decision.** Spotless with Google Java Format for the backend (checked in `mvn verify`), and
  ESLint (angular-eslint, with template accessibility rules) plus Prettier for the frontend.
  Conventional Commits for messages.
- **Consequences.** Style debates are settled by tooling, and CI rejects unformatted code.
