# Taskflow

Task management with priorities and reminders, built on Spring Boot and Angular.

> **Status:** early development. The repository currently contains the project foundation (monorepo layout, build tooling and a Docker Compose environment). Task management and authentication are being built next; see the roadmap below.

## Repository layout

| Path | Description |
|---|---|
| `taskflow-back/` | REST API: Spring Boot 3, Java 21, Spring Data JPA, MySQL |
| `taskflow-web/` | Single-page application: Angular 18 (standalone components) |
| `docker-compose.yml` | Local environment: MySQL, backend and web |
| `.env.example` | Documented environment variables |

## Getting started

### With Docker Compose

```bash
cp .env.example .env   # then set real passwords
docker compose up --build
```

- Web: http://localhost:4200
- API: http://localhost:8080

### Without Docker

Requirements: JDK 21, Node.js 22 and a running MySQL 8 instance.

```bash
# Backend (reads DB_URL, DB_USERNAME and DB_PASSWORD from the environment)
cd taskflow-back
./mvnw spring-boot:run

# Frontend (proxies /api to http://localhost:8080)
cd taskflow-web
npm install
npm start
```

## Tests

```bash
cd taskflow-back && ./mvnw verify
cd taskflow-web && npm test
```

## Roadmap

- [x] Monorepo foundation and local environment
- [ ] Authentication (registration, login, JWT)
- [ ] Task CRUD with priorities and due dates
- [ ] Reminders
- [ ] Web UI for authentication and tasks
- [ ] CI, linting and full documentation

## License

Released under the [MIT License](./LICENSE).
