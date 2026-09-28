# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-09-28

Initial release.

### Added

- Spring Boot 3 REST API with feature-oriented Clean Architecture (`auth`, `tasks`) and a shared kernel.
- User registration and login with BCrypt password hashing and stateless JWT access tokens.
- Task management: create, read, update, change status and delete, with priorities, statuses, due
  dates, overdue flag, filtering, text search, sorting and pagination.
- Task reminders delivered by a scheduler through a replaceable `ReminderNotifier` port.
- RFC 7807 error responses, bean validation, OpenAPI documentation and Actuator health probes.
- MySQL schema managed by Flyway migrations, validated by Hibernate at startup.
- Angular 18 single-page application with sign-in, registration and a task list and form, using
  standalone components, signals, typed reactive forms, lazy-loaded routes, guards and an HTTP
  interceptor.
- Docker Compose environment (MySQL, API, nginx-served web app) with a strict Content-Security-Policy.
- Unit, integration, component and architecture tests (ArchUnit).
- Spotless, ESLint and Prettier; GitHub Actions workflows, issue forms, pull request template and
  Dependabot configuration.
- Documentation: README, architecture guide, development guide and decision records.

### Security

- No default secrets: the JWT secret and database password must be provided by the environment.
- A task owned by another user is reported as not found, so identifiers cannot be probed.

[Unreleased]: https://github.com/eliangilsierra/taskflow/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/eliangilsierra/taskflow/releases/tag/v0.1.0
