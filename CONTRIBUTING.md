# Contributing to Taskflow

Thanks for your interest in improving Taskflow! This guide explains how to set up the project, what
is expected from a contribution, and how changes are reviewed.

## Getting set up

Follow the [development guide](docs/development.md). In short: JDK 21, Node.js 22, and either Docker
or a local MySQL 8.

## Workflow

1. Open an issue (or comment on an existing one) before starting non-trivial work.
2. Fork the repository and create a branch from `main`, for example `feat/task-labels`.
3. Make your change, with tests.
4. Run the checks listed below.
5. Open a pull request using the template and link the issue.

## Before you push

```bash
cd taskflow-back && ./mvnw verify
cd taskflow-web && npm run lint && npm run format:check && npm run test:ci && npm run build
```

CI runs the same commands. `./mvnw spotless:apply` and `npm run format` fix formatting.

## Coding guidelines

- Respect the architecture in [docs/architecture.md](docs/architecture.md). `ArchitectureTest`
  fails the build when a dependency points the wrong way.
- Keep business rules in the `domain` layer and free of Spring, JPA and HTTP types.
- Prefer small, focused methods and explicit names. Do not leave dead code or unresolved TODOs.
- Validate input at the API boundary and keep error messages free of internal details.
- Never commit secrets. Configuration comes from environment variables and is documented in
  `.env.example`.
- Every behavior change needs a test. Bug fixes should include a test that fails without the fix.
- Database changes go in a new Flyway migration; never edit one that was already merged.

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/) in English, in the imperative mood:

```text
feat(back): add task labels
fix(web): keep filters when returning from the edit form
docs: explain reminder configuration
```

Common types: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`. Use the scopes
`back` and `web` when a change affects only one side. Keep commits atomic: one logical change each.

## Pull requests

- Keep them focused and reasonably small.
- Describe what changed and why, and how you tested it.
- Update the documentation and [CHANGELOG.md](CHANGELOG.md) (under `Unreleased`) when behavior
  changes.
- Be responsive to review comments; maintainers may ask for changes before merging.

## Suggested labels

| Label | Meaning |
|---|---|
| `bug` | Something is broken |
| `enhancement` | New feature or improvement |
| `documentation` | Docs only |
| `good first issue` | Small, well-scoped, beginner friendly |
| `help wanted` | Maintainers would welcome a contribution |
| `backend`, `frontend`, `devops` | Area of the change |
| `security` | Security-related (report vulnerabilities privately, see [SECURITY.md](SECURITY.md)) |
| `dependencies` | Dependency updates (applied by Dependabot) |
| `breaking change` | Requires a major version bump |
| `blocked`, `needs-info` | Waiting on something |

## Code of conduct

Be respectful and constructive. Harassment and personal attacks are not tolerated.
