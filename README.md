# Home Jobs Tracker

A self-hosted app for tracking household jobs/projects — repairs, renovations, contractor
work — with quoted-vs-actual cost and time comparison. Two pieces:

- **[`backend/`](./backend)** — a Kotlin + Ktor REST API backed by PostgreSQL, meant to run as a
  small Docker Compose stack on a home server behind a reverse proxy (Caddy/Traefik).
- **[`android/`](./android)** — a native Kotlin (Jetpack Compose) client for it.

Each has its own README with full setup instructions, architecture notes, and test instructions.
This file is the map.

## Quick start

```bash
cp .env.example .env
# edit .env — set POSTGRES_PASSWORD and API_KEY (openssl rand -hex 32 for the latter)

docker compose up --build
curl http://localhost:8080/health

BASE_URL=http://localhost:8080 API_KEY=<your API_KEY> ./backend/scripts/seed.sh
```

Then open the `android/` project in Android Studio, run it, and enter your server URL + API key
in the app's Settings screen.

## Layout

```
/backend    Ktor API, Postgres schema (Flyway), tests, Dockerfile — see backend/README.md
/android    Jetpack Compose app (MVVM, Room, Retrofit, Hilt) — see android/README.md
docker-compose.yml   Postgres + API, named volumes, health checks, restart policies
.env.example         all backend configuration, no secrets committed
```

## Data model

- **Job**: title, category, room/location, vendor + contact, status (`quoted → scheduled →
  in_progress → done → cancelled`), quoted/actual cost, predicted/actual hours, scheduled/
  completed/warranty dates, payment status + method. `costVariance` and `timeVariance`
  (`actual − quoted`) are always computed, never stored.
- **JobNote**: a timestamped timeline entry on a job; cascade-deleted with the job.
- **Attachment**: table exists for photo references (stored on disk, not in the DB); upload/serve
  endpoints are the one item left as a stretch follow-up (see backend README).

## Verification status

The backend was built and its test suite run in the sandbox that produced this repo: `./gradlew
build` and `./gradlew test` both pass. Two things were **not** runnable there for reasons
external to the code and are the natural next steps when you set this up for real:

- `docker compose up` and `./gradlew integrationTest` (Testcontainers) — both need to pull
  container images from a registry, which that sandbox's network policy blocked.
- The entire `android/` module — that sandbox had no Android SDK and no access to Google's Maven
  repository (required for the Android Gradle Plugin, Room, Compose, etc.). It was written
  carefully against known library APIs but needs a real build (Android Studio, or a machine with
  the Android SDK and normal internet access) as its first compilation check.

Both READMEs call this out again in context. Treat "run `docker compose up`" and "open `android/`
in Android Studio" as the two remaining acceptance steps for this project, not optional extras.

## License

No license file is included — add one if you plan to share this beyond your own household.
