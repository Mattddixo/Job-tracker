# Home Jobs Tracker — Android

Native Kotlin client for the Home Jobs Tracker backend: job list with filtering/sorting, job
detail with computed cost/time variance and a notes timeline, a create/edit form, and a settings
screen to point the app at your own server.

> **Build status**: this module was written in a sandboxed environment with no Android SDK and
> no network access to Google's Maven repository (`dl.google.com` / `maven.google.com` are
> blocked by that sandbox's egress policy), so **it has not been compiled or run here**. The
> backend was fully build- and test-verified; this module was written carefully against known
> Ktor/Room/Retrofit/Hilt/Compose APIs but needs a real build (Android Studio or a machine with
> the Android SDK + normal internet access) as its first verification step. Please run
> `./gradlew build` and `./gradlew test` after cloning, before relying on it.

## Stack

- Jetpack Compose (Material 3) for UI, single-activity
- MVVM: `ViewModel` + `StateFlow`, no business logic in composables
- Kotlin Coroutines/Flow throughout
- Retrofit + OkHttp for networking, kotlinx.serialization for JSON
- Room as the local cache (offline-friendly reads)
- Hilt for dependency injection
- DataStore (Preferences) for settings (server URL, API token)

## Project layout

```
app/src/main/kotlin/com/homejobs/android/
  domain/            pure Kotlin models + repository interfaces — no Android/Room/Retrofit types
  data/
    local/db/         Room entities, DAOs, database
    local/datastore/  settings (server URL, API token) persistence
    remote/           Retrofit API interface, DTOs, dynamic base-URL interceptor
    repository/       repository implementations + DTO/Entity/domain mappers
  di/                 Hilt modules (network, database, repositories, app-scope)
  ui/
    jobs/list/        job list screen + ViewModel
    jobs/detail/       job detail + timeline screen + ViewModel
    jobs/form/         create/edit form + ViewModel
    settings/          settings screen + ViewModel
    navigation/        single-activity NavHost + routes
    theme/             Material 3 theme
    common/            shared loading/empty/error composables
app/src/test/kotlin/com/homejobs/android/
  fakes/              hand-written fakes for JobRepository, DAOs, API service
  viewmodel/          ViewModel unit tests
  repository/         JobRepositoryImpl unit tests
```

## Architecture notes

- **Repository pattern, Room as source of truth for reads.** `observeJobs`/`observeJob`/
  `observeNotes` always return what's cached on-device; `refreshJobs()`/`refreshJob()`/
  `refreshNotes()` hit the network and, on success, update the cache so the Flow-based UI picks
  up the change automatically. This is what makes the list/detail screens work offline once
  they've been loaded at least once.
- **Writes are network-first**, not queued for later sync: `createJob`/`updateJob`/`deleteJob`/
  `addNote`/`deleteNote` call the API immediately and return `Result<T>`, updating the local cache
  only on success. A full offline write queue (create/edit while offline, sync later, handle
  conflicts) is real complexity that didn't seem worth it for a single-user household tool talking
  to a server on the same home network — this can be added later without changing the repository
  interface's shape.
- **Filtering happens in SQL (Room), sorting happens in memory.** Room can't bind a column name
  as an `ORDER BY` parameter, and a household's job list is never going to be large enough for
  that to matter — `JobRepositoryImpl` fetches the filtered rows and applies a `Comparator` in
  Kotlin.
- **Dynamic base URL.** Retrofit is built once against a placeholder base URL;
  `DynamicBaseUrlInterceptor` rewrites the scheme/host/port (and preserves any reverse-proxy path
  prefix from the configured server URL) and attaches the bearer token on every request, reading
  the latest value from a `StateFlow` backed by DataStore. This is what lets the Settings screen
  change the server URL/token at runtime with no app restart.
- **`Result<T>` over exceptions crossing layers.** Repository methods that talk to the network
  return `Result<T>`; ViewModels turn a `Result.failure` into a user-facing `errorMessage` string
  rather than letting a raw `IOException`/`HttpException` reach the UI layer.

## Configuring the app

Open **Settings** (gear icon on the job list) and enter:

- **Server URL** — e.g. `https://jobs.example.com` if you've put the backend behind a reverse
  proxy, or `http://<your-server-ip>:8080` for direct access on your home network.
- **API key** — the same value you set as `API_KEY` in the backend's `.env`.

Both are stored in DataStore on-device; nothing is hardcoded in the app.

**Cleartext (plain `http://`) traffic is allowed** (`android:usesCleartextTraffic="true"` in the
manifest) — Android blocks it by default from API 28 on. This app is meant to be pointed at a
server reachable only over Tailscale (or another private/VPN network), where the transport is
already encrypted at the WireGuard layer even though the URL says `http://`; Android has no way
to know that; it just sees a non-TLS scheme and refuses it. If you ever point this app at a
server over the open internet, use `https://` (e.g. via a Caddy/Traefik reverse proxy) instead —
cleartext being *allowed* doesn't make plain HTTP over the public internet safe.

## Running it

1. Open the `android/` directory in Android Studio (Koala or newer recommended).
2. Let Gradle sync — this needs internet access to `google()`/`mavenCentral()` the first time.
3. Run the `app` configuration on an emulator or device (minSdk 26 / Android 8.0+).
4. Point it at a running backend via the Settings screen (see above).

From the command line, once you have the Android SDK installed and `ANDROID_HOME` set:

```bash
cd android
./gradlew assembleDebug
```

**Note**: unlike `backend/`, this module does not include a committed Gradle wrapper
(`gradlew`/`gradle-wrapper.jar`) — generating one requires resolving the Android Gradle Plugin
from `google()`, which wasn't reachable in the sandbox this was built in. Android Studio will
generate the wrapper for you automatically on first sync, or run `gradle wrapper --gradle-version
8.10.2` once yourself with a local Gradle install and Android SDK/network access.

## Tests

```bash
cd android
./gradlew test
```

Unit tests use hand-written fakes for `JobRepository`, `JobDao`/`JobNoteDao`, and
`HomeJobsApiService` (rather than Robolectric/instrumentation), so they run as plain JVM tests —
no emulator, no Android SDK at test-run time. Covered:

- `JobRepositoryImplTest` — refresh populates/updates the cache, a failed refresh preserves
  stale cached data, server-side deletions are reflected on the next refresh, create/delete/notes
  round-trip through the fake API and cache.
- `JobListViewModelTest` — initial refresh, refresh-failure error surfacing, status filtering,
  delete delegation.
- `JobFormViewModelTest` — validation blocks save on bad input, valid input creates a job and
  fires the callback, editing pre-populates the form from the repository.
