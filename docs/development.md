# Development guide

## Prerequisites

| Tool | Version | Needed for |
|---|---|---|
| JDK | 21 | backend |
| Node.js | 22 | frontend |
| MySQL | 8.x | running the backend outside Docker |
| Docker with Compose v2 | recent | the one-command setup |

Maven and the Angular CLI do not need to be installed: use `./mvnw` and `npx ng`.

## Configuration

All configuration is read from environment variables. `.env.example` lists every variable used by
Docker Compose; copy it to `.env` (which is git-ignored) and set real values.

| Variable | Used by | Default | Description |
|---|---|---|---|
| `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` | Compose (MySQL) | — | Database bootstrap |
| `DB_URL` | backend | `jdbc:mysql://localhost:3306/taskflow` | JDBC URL |
| `DB_USERNAME` | backend | `taskflow` | Database user |
| `DB_PASSWORD` | backend | **required** | Database password |
| `JWT_SECRET` | backend | **required** | HMAC signing secret, at least 32 characters |
| `JWT_TTL` | backend | `1h` | Access token lifetime (Spring duration syntax) |
| `SERVER_PORT` | backend | `8080` | HTTP port |
| `REMINDERS_ENABLED` | backend | `true` | Turns the reminder scheduler on or off |
| `REMINDERS_INTERVAL` | backend | `PT1M` | Delay between reminder runs (ISO-8601 duration) |
| `BACK_PORT`, `WEB_PORT` | Compose | `8080`, `4200` | Published host ports |

Generate a strong secret with `openssl rand -base64 48`.

## Running the stack

### Everything in Docker

```bash
cp .env.example .env
docker compose up --build
```

Open <http://localhost:4200>. The API is on <http://localhost:8080>.

### Backend and frontend on the host

Start a MySQL 8 instance (for example `docker compose up mysql`), then:

```bash
export DB_URL=jdbc:mysql://localhost:3306/taskflow \
       DB_USERNAME=taskflow DB_PASSWORD=change-me \
       JWT_SECRET="$(openssl rand -base64 48)"
cd taskflow-back && ./mvnw spring-boot:run
```

```bash
cd taskflow-web && npm install && npm start   # http://localhost:4200, proxies /api to :8080
```

Interactive API documentation is served at <http://localhost:8080/swagger-ui.html> and the raw
OpenAPI document at `/v3/api-docs`.

## Quality checks

```bash
# Backend: compile, check formatting (Spotless), run all tests
cd taskflow-back && ./mvnw verify
./mvnw spotless:apply            # fix formatting

# Frontend
cd taskflow-web
npm run lint                     # angular-eslint
npm run format:check             # Prettier (use `npm run format` to fix)
npm run test:ci                  # Karma, headless Chrome
npm run build
```

CI runs exactly these commands (see `.github/workflows`).

## Common tasks

### Add a database change

1. Create `taskflow-back/src/main/resources/db/migration/V<next>__<description>.sql`. Never edit a
   migration that has already been merged.
2. Keep the SQL portable between MySQL and H2's MySQL mode (tests run on H2).
3. Update the JPA entity. `ddl-auto=validate` fails at startup if entity and schema disagree.

### Add a feature module

Follow the layout in [architecture.md](architecture.md): `domain` (model and ports), `application`
(use cases), `infrastructure` (adapters) and `api` (controller and DTOs), plus unit tests for the
domain and use cases and a MockMvc test for the endpoints. On the frontend add a lazy-loaded folder
under `features/` with its own routes.

### Replace the reminder delivery

Implement `ReminderNotifier` (for example with Spring Mail) as a Spring bean and remove
`LoggingReminderNotifier`.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| Backend fails with `Could not resolve placeholder 'JWT_SECRET'` | Set `JWT_SECRET` (and `DB_PASSWORD`). |
| Backend fails with `must be at least 32 characters` | The JWT secret is too short. |
| `Schema-validation: wrong column type` | An entity and its Flyway migration disagree. |
| Every request from `ng serve` returns 5xx | The backend is not running on port 8080. |
| Signed out unexpectedly | The access token expired (`JWT_TTL`) and the API answered `401`. |
