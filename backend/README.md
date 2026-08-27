# Home Jobs Tracker — Backend

A small Kotlin + Ktor REST API for tracking household jobs/projects (repairs, renovations,
contractor work) with quoted-vs-actual cost and time comparison. Built for a single
household running it on their own hardware behind a reverse proxy — not a multi-tenant SaaS.

## Why Kotlin + Ktor (not Go)

The Android app is also Kotlin, so sharing one language across the stack means one toolchain,
one mental model for data classes/serialization, and the ability to eventually share DTO-shaped
code if useful. Ktor + Exposed + Flyway + Postgres is still a small, well-supported stack with
few moving parts — appropriate for a homelab deployment. Go would have been a perfectly
reasonable alternative (smaller memory footprint, single static binary), but the shared-language
win mattered more here since one person is maintaining both halves.

## Tech stack

- **Ktor** (Netty engine) for the HTTP server
- **Exposed** as the SQL layer over **PostgreSQL** (via HikariCP pooling)
- **Flyway** for schema migrations (`src/main/resources/db/migration`) — no hand-run SQL, ever
- **kotlinx.serialization** for JSON
- Single static **API key** (`Authorization: Bearer <API_KEY>`) — appropriate for one household,
  not over-engineered into a full user/session system
- **JUnit 5** for unit tests, **Testcontainers** for integration tests against a real Postgres

## Project layout

```
backend/
  src/main/kotlin/com/homejobs/
    Application.kt          entry point, wires everything together
    config/                  environment-variable configuration
    db/                      DataSource + Flyway + Exposed table definitions
    domain/                  data classes, enums, request validation, errors
    repository/              Exposed-backed CRUD + filtering/sorting
    routes/                  Ktor routes (/api/v1/jobs, /api/v1/jobs/{id}/notes, /health)
    plugins/                 serialization, logging, auth, error handling, routing wiring
  src/main/resources/db/migration/   Flyway SQL migrations
  src/test/kotlin/           unit tests (validation, computed variance fields)
  src/integrationTest/kotlin/ Testcontainers-backed API integration tests
  openapi.yaml               hand-written OpenAPI 3 spec
  Dockerfile
```

## Data model

- **Job** — title, category, room/location, vendor name + contact, status
  (`quoted → scheduled → in_progress → done → cancelled`), quoted/actual cost, predicted/actual
  hours, scheduled/completed/warranty dates, payment status + method, timestamps. `costVariance`
  and `timeVariance` (`actual − quoted`) are always computed on read from the underlying columns —
  they're never stored, so they can't drift out of sync with the source values.
- **JobNote** — a timestamped timeline entry attached to a job. Deleting a job cascades to its
  notes at the database level (`ON DELETE CASCADE`).
- **Attachment** — table exists (photo file references, stored on disk under `ATTACHMENTS_DIR`,
  not in the DB) but the upload/serving endpoints are not implemented yet; this is the one
  stretch item left as a follow-up.

## Running it

### With Docker Compose (recommended)

From the **repo root** (not this directory):

```bash
cp .env.example .env
# edit .env: set POSTGRES_PASSWORD and API_KEY to real values
#   openssl rand -hex 32   # good way to generate API_KEY

docker compose up --build
```

This brings up Postgres (named volume `db-data`) and the API (named volume `attachments`), with
health checks and `restart: unless-stopped`. The API binds to `127.0.0.1:${HTTP_PORT}` by
default — put Caddy/Traefik in front of it for real exposure; don't publish it directly to `0.0.0.0`
unless you know you want that.

Check it's up:

```bash
curl http://localhost:8080/health
```

Seed some demo data:

```bash
BASE_URL=http://localhost:8080 API_KEY=<your API_KEY> ./scripts/seed.sh
```

### Locally (without Docker)

You need a Postgres instance reachable from your machine (e.g. `docker run -e
POSTGRES_PASSWORD=dev -e POSTGRES_DB=homejobs -e POSTGRES_USER=homejobs -p 5432:5432 -d
postgres:16-alpine`), then:

```bash
cd backend
export DATABASE_URL=jdbc:postgresql://localhost:5432/homejobs
export DATABASE_USER=homejobs
export DATABASE_PASSWORD=dev
export API_KEY=dev-key
./gradlew run
```

## Configuration

All configuration is via environment variables (see `.env.example` at the repo root) — nothing
is hardcoded and no secrets are committed:

| Variable | Purpose |
|---|---|
| `HTTP_PORT` | Port the API listens on (default `8080`) |
| `DATABASE_URL` | JDBC URL, e.g. `jdbc:postgresql://db:5432/homejobs` |
| `DATABASE_USER` / `DATABASE_PASSWORD` | Postgres credentials |
| `API_KEY` | Static bearer token clients must send |
| `CORS_ALLOWED_HOST` | Optional, for browser-based clients |
| `ATTACHMENTS_DIR` | Where photo attachments will be stored on disk |

## API

Versioned REST API under `/api/v1`, JSON in and out. Full spec in [`openapi.yaml`](./openapi.yaml)
— paste it into <https://editor.swagger.io> or any OpenAPI viewer for interactive docs.

- `GET /health` — unauthenticated liveness probe (used by the Docker health check)
- `GET/POST /api/v1/jobs`, `GET/PUT/DELETE /api/v1/jobs/{id}` — filter by `status`/`category`/
  `location`; sort via `sortBy`/`sortDir`
- `GET/POST /api/v1/jobs/{id}/notes`, `DELETE /api/v1/jobs/{id}/notes/{noteId}` — the timeline

Every request under `/api/v1` requires `Authorization: Bearer <API_KEY>`. Validation failures
return `400` with a structured body (`{"error", "message", "details": [...]}`); missing
resources return `404` in the same shape; unexpected errors return `500` without leaking
internals, and are logged server-side.

## Tests

```bash
./gradlew test            # unit tests — validation rules, computed variance fields
./gradlew integrationTest  # full API tests against a real Postgres via Testcontainers
```

`integrationTest` is a separate Gradle source set/task, **not** wired into `build`/`check`,
because it needs a working Docker daemon to spin up Postgres containers — that's a deliberate
choice so a plain `./gradlew build` never needs Docker.

> **Note on this repo's own CI/verification**: this backend was built and its unit tests were
> run in a sandboxed environment without Docker registry access, so `./gradlew build` and
> `./gradlew test` are confirmed passing here, but `docker compose up` and `./gradlew
> integrationTest` have **not** been run end-to-end in that same sandbox (both need to pull
> images from Docker Hub, which wasn't reachable there). Please run both once when you first set
> this up on your actual server — that's the real verification.

## Architectural notes

- **Auth**: a hand-rolled Ktor `intercept` checking a static bearer token, rather than Ktor's
  `Authentication` plugin or JWT. For one household user there's no session and no user table —
  a JWT's claims/expiry buy nothing over comparing one secret directly.
- **status/payment_status columns**: `TEXT` + `CHECK` constraint rather than a native Postgres
  `ENUM` type. Exposed/JDBC need explicit casts on every bind parameter for a native enum column;
  `TEXT` + `CHECK` gets the same integrity guarantee without that friction.
- **Timestamps**: stored as `TIMESTAMP` (no timezone) and always treated as UTC end-to-end, to
  avoid JDBC session-timezone conversion surprises with `TIMESTAMPTZ`.
- **costVariance/timeVariance**: computed properties on the `Job` domain object, never persisted
  columns — there is exactly one source of truth for quoted/actual values.
