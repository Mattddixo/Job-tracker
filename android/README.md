# Home Jobs Tracker — Android

A standalone native Kotlin app for tracking household jobs/projects: an Active/Completed/All
job list sorted however you like, a job detail screen with computed cost/time variance and a
photo-backed notes timeline, and a create/edit form organized the way a project actually gets
managed (details → vendor → status & schedule → cost tracking → time tracking → payment).

Everything is local. There is no backend, no account, no network permission in the manifest —
Room is the only store.

> **Build status**: this module was written in a sandboxed environment with no Android SDK and
> no network access to Google's Maven repository (`dl.google.com` / `maven.google.com` are
> blocked by that sandbox's egress policy), so **it has not been compiled or run here**. It was
> written carefully against known Room/Hilt/Compose/Coil APIs and reviewed line-by-line, but
> needs a real build (Android Studio, or a machine with the Android SDK and normal internet
> access) as its first verification step. Please run `./gradlew build` and `./gradlew test`
> after cloning, before relying on it.

## Stack

- Jetpack Compose (Material 3) for UI, single-activity
- MVVM: `ViewModel` + `StateFlow`, no business logic in composables
- Kotlin Coroutines/Flow throughout
- Room as the **only** persistence layer (no network, no remote sync)
- Hilt for dependency injection
- Coil for loading locally-stored photo files into Compose

## Project layout

```
app/src/main/kotlin/com/homejobs/android/
  domain/            pure Kotlin models + the JobRepository interface — no Room/Android types
  data/
    local/db/         Room entities, DAOs, database (jobs, job_notes, photos)
    local/photo/       PhotoStorage — copies camera/gallery images into app-private storage
    local/prefs/       ThemePreferences — persisted theme mode + color palette choice
    repository/       JobRepositoryImpl (Room-backed) + Entity/domain mappers
  di/                 Hilt modules (database, repository)
  ui/
    jobs/list/        job list screen + ViewModel (Active/Completed/All tabs, sort)
    jobs/detail/       job detail + notes timeline + photo capture/viewer + ViewModel
    jobs/form/         sectioned create/edit form + ViewModel
    jobs/photos/       all-photos grid for a job + ViewModel
    jobs/picker/       job picker for linking to an existing Job Jar job + ViewModel
    stats/             cost-by-payment-method stats + manage payment methods + ViewModel
    appearance/        Light/Dark/Custom toggle + color wheel picker screen
    navigation/        single-activity NavHost + routes + DeepLink (Job Jar handoff)
    theme/             custom Material 3 theme — Light/Dark schemes, Custom color derivation, typography
    common/            shared loading/empty/error/photo-viewer composables, date formatting,
                       EnumDropdown, CrossAppLink (Job Jar intents)
app/src/test/kotlin/com/homejobs/android/
  fakes/              hand-written fakes for JobRepository and the Room DAOs
  viewmodel/          ViewModel unit tests
  repository/         JobRepositoryImpl unit tests
```

## Architecture notes

- **Room is the only store.** No network layer, no `Result<T>` wrapping for network failure —
  repository methods either return the value directly or, for the rare genuinely-unexpected
  local error, let the exception propagate to the caller (ViewModels catch it where a
  user-facing message makes sense, e.g. saving a job).
- **Job/note/photo ids are on-device auto-generated** (Room `autoGenerate = true`), not
  server-assigned. Installing this version over an older client/server build wipes existing
  local data — see the git history if you need the previous schema.
- **Active/Completed/All is a UI-level grouping, not a database query.** `JobListTab` (in
  `ui/jobs/list`) buckets by status purely in `JobListViewModel`, on top of whatever
  `observeJobs(filter)` already returned — a household's job list is small enough that doing
  this in memory is simpler than teaching Room to filter on a set of statuses. Defaults to
  Active so finished jobs don't bury what's still open.
- **Sorting is also in-memory**, for the same reason, and now includes a `JobSortField` picker
  in the list screen (not just direction) — sort by scheduled date to see what's coming up
  next, by cost/time variance to see what's over budget, etc.
- **Photos are copied into app-private storage, never referenced by their original Uri.** A
  gallery pick's `content://` Uri is only guaranteed readable briefly (no persisted
  permission); a camera capture is written by a different app entirely. `PhotoStorage` copies
  (or, for the camera, directs the capture directly) into `context.filesDir/photos/`, and only
  that stable file path is ever stored in Room. A `FileProvider` (declared in the manifest,
  paths in `res/xml/file_paths.xml`) is what lets the camera app write into that private
  directory in the first place.
- **Notes can be photo-only.** A note requires either a non-blank body or at least one photo,
  not both — e.g. attaching a "before" photo with no comment is a valid timeline entry.
- **Job-form number fields keep their own text buffer.** Deriving the displayed text from the
  parsed `Double` on every recomposition (e.g. `1.0.toString()`) would rewrite "1" to "1.0"
  mid-keystroke, making it impossible to type "12.5". `NumberField` seeds a local `String` once
  (the form blocks on a loading state until any existing job data has arrived, so the seed
  value is correct in both create and edit mode) and only pushes the parsed value up to the
  ViewModel.
- **Date fields use a real `DatePicker`**, opened via an invisible clickable `Box` layered over
  a read-only `OutlinedTextField` — attaching `.clickable` directly to a read-only text field is
  a well-known Compose gotcha (the field still consumes the tap internally for its own
  focus/cursor handling) — and round-trip through UTC-midnight epoch millis (the convention
  `DatePicker` itself uses) to avoid the classic off-by-one-day bug from mixing in the device's
  local time zone.
- **Cost/time variance is shown live in the form itself**, not just on the detail screen —
  typing an actual cost immediately shows how far over/under quote it is, in the same
  actual-minus-quoted convention as `Job.costVariance`.
- **Tapping any photo opens a full-screen, swipeable viewer** (`ui/common/PhotoViewerDialog.kt`,
  built on `HorizontalPager`). From a note's timeline, swiping browses that note's own photos;
  from the all-photos grid, it browses every photo on the job. Actions: Share (Android's normal
  share sheet, via the same `FileProvider` used for camera capture), Save to gallery (copies into
  the device's public Pictures folder via MediaStore, so it shows up in the regular Photos app
  independent of this one — API 26-28 requests `WRITE_EXTERNAL_STORAGE` at runtime the first
  time, API 29+ needs no permission at all), and a "view all photos" button that jumps to the
  full grid landing on the photo you were just viewing. The note's text shows as a togglable
  caption (an eye icon hides/shows it, e.g. when it's covering part of the photo).
- A "view all photos" icon on the job detail screen opens `JobPhotosScreen`, a grid of every
  photo across all of that job's notes (newest first) — useful once a job has enough notes that a
  specific photo isn't easy to find by scrolling the timeline.
- **Notes are editable after saving**, not just deletable — a pencil icon puts a note's text into
  an inline edit field (Save/Cancel), and the same camera/gallery buttons used when composing a
  new note let you attach more photos to an already-saved one; removing a photo already worked
  from either place.
- **The theme is Light, Dark, or Custom — no Material You.** `ui/theme/Color.kt` defines a
  hand-picked slate-teal/sage/clay/warm-paper palette for Light and Dark, instead of stock
  Material swatches; Android 12+ wallpaper-extracted dynamic color is deliberately never used.
  Over/under quote and over/under time read the active scheme's tertiary/secondary colors
  instead of a plain error-red/primary split. The theme icon in the job list's top bar opens an
  **Appearance** screen (`ui/appearance/`) with a Light/Dark/Custom toggle; Custom reveals three
  swatches (primary/secondary/accent) you pick from a hue/saturation wheel plus a brightness
  slider (`ColorWheelPicker.kt`) — `ui/theme/CustomColors.kt` derives full on-color/container
  pairs from each picked color via HSL lightness shifts (light mode uses the picked color as-is
  with a pastel container; dark mode brightens it for contrast against the dark surface, same
  convention as the built-in Dark scheme). The chosen mode and, once picked, the custom colors
  persist across restarts via `ThemePreferences` (`SharedPreferences`-backed; Light is the
  default).
- **Payment methods are a small managed list, not free text.** A job's "payment method" is a
  `paymentMethodId` foreign key into a `payment_methods` table (`PaymentMethod`: name + an
  optional `maxCredit` — presence of a max credit is what makes something "a card," no separate
  flag), picked from a dropdown in the job form (`PaymentMethodField.kt`) that ends with an
  "add new" entry so a card can be created without leaving the form. Deleting a payment method
  uses `onDelete = SET_NULL` (not `CASCADE`) — jobs using it just fall back to "Unassigned"
  rather than being deleted; the delete confirmation says how many jobs that'll affect.
- **Stats and Payment Methods (`ui/stats/`) are two separate screens, each with their own icon
  in the job list's top bar** (a credit-card icon and a bar-chart icon), rather than one nested
  inside the other — payment methods are something you set up once and occasionally edit, not a
  sub-view of looking at cost totals, so burying "Manage" behind an edit icon on the Stats screen
  made it hard to find. `StatsScreen` rolls up job costs per payment method, split into
  Paid/Partial/Unpaid using each job's `actualCost` — a quote isn't money that's actually gone
  out on that method yet, so jobs without an actual cost count toward a method's job count but
  not its dollar totals — with a "$X of $Y limit used" bar against `maxCredit`.
  `PaymentMethodsScreen` is the add/edit/delete CRUD list, sharing the same `StatsViewModel`
  (and thus the same job-count-per-method data, for its delete confirmation) as `StatsScreen`.

## Running it

1. Open the `android/` directory in Android Studio (Koala or newer recommended).
2. Let Gradle sync — needs normal internet access the first time (this is the first real
   compile check; see the build-status note above).
3. Run the `app` configuration on an emulator or device (minSdk 26 / Android 8.0+).

From the command line, once you have the Android SDK installed and `ANDROID_HOME` set:

```bash
cd android
./gradlew assembleDebug
```

**Note**: this module does not include a committed Gradle wrapper (`gradlew`/
`gradle-wrapper.jar`) — generating one requires resolving the Android Gradle Plugin from
`google()`, which wasn't reachable in the sandbox this was built in. Android Studio generates
the wrapper automatically on first sync, or run `gradle wrapper --gradle-version 8.10.2` once
yourself with a local Gradle install and Android SDK/network access.

## Permissions

None beyond what's needed to write photo files the app itself creates. No `INTERNET`
permission — there's nothing to talk to. The camera and gallery pickers are launched via
system intents (`ACTION_IMAGE_CAPTURE`, the Android Photo Picker), so neither needs a runtime
permission request from this app.

## Interop with Job Jar

A job here can be **linked** to a task in [Job Jar](https://github.com/Mattddixo/Jobjar) (a
separate, unrelated app for tracking chores to draw by time budget) — a real, symmetric,
duplicate-proof two-way link, not a one-shot copy — via implicit `ACTION_VIEW` intents against a
custom URI scheme each app declares, the standard same-device mechanism for two local-only apps
(no server, no shared account) to exchange data. `ui/navigation/DeepLink.kt` parses the incoming
Uri; `MainActivity` calls `navController.navigate(...)` explicitly (or, for the `linked`
callback, updates the repository directly) for both a cold start and an already-running instance
(`onNewIntent`), rather than relying on Navigation-Compose's declarative deep-link auto-matching.
Every one of those `navigate(...)` calls passes `launchSingleTop = true` — repeatedly bouncing
back and forth via "Open in..." always re-navigates to the exact same destination this app's own
back stack was already sitting on (nothing pops it while this app is merely backgrounded, not
finished), so without this each round trip would push another duplicate copy on top, and Back
would have to be pressed once per bounce before it did anything visible.

- **Once `job.linkedJobJarId` is set, the job detail screen shows exactly one button: "Open in
  Job Jar"** (`jobjar://job/{id}`) — true two-way navigation, usable from whichever side
  originated the link. Until then, it shows two:
  - **"Send to Job Jar"** —
    `jobjar://newjob?title=...&category=...&sourceId=...&estimatedMinutes=...&scheduledDate=...`
    pre-fills a new Job Jar task's title, category, estimated duration, and (if set) scheduled
    date. `predictedHours` converts to `estimatedMinutes` (× 60); `scheduledDate` — this app's own
    field is date-only — carries over as-is and Job Jar defaults it to 9 AM local time once it
    picks a time-of-day. Nothing is auto-saved — it lands on Job Jar's own create form, reviewed
    and saved like any other task, and only takes effect (including writing a real calendar
    event) once that form is actually saved. On save, Job Jar fires
    `hometracker://linked?jobId=...&otherId=...` back, so *this* job also learns the new task's
    id — the origin isn't left one-way blind to what it just created.
  - **"Link to existing Job Jar job"** — `jobjar://pickjob?returnJobId=...` opens a minimal
    picker over Job Jar's own job list (top-level jobs *and* subtasks — see below); picking one
    sets the link from that side and fires the same `linked` callback back.
- Both actions disappear the instant a link exists — **a linked job can't be sent or linked
  again**, so there's no fresh code path that could create a second link for one job. That's not
  quite the same as saying a link can never be *wrong*, though: the two sides can still end up
  desynced (one side's own field cleared by a bug, or a linked job deleted and its id later
  reused for something unrelated), and neither app can detect that on its own. So
  `ui/jobs/picker/JobPickerScreen.kt` (this app's own picker, reached via Job Jar's mirror-image
  "Link to existing Job Tracker job" button) deliberately does **not** exclude already-linked
  jobs — it lists every job, flags ones that already point elsewhere, and picking one anyway
  overwrites that stale pointer, since re-picking is the only reliable way to fix a desync once
  the two sides disagree.
- Either "Open"/"Send"/"Link" button shows a "Job Jar isn't installed" toast instead of crashing
  if the target app isn't present (`ActivityNotFoundException`, in `ui/common/CrossAppLink.kt`).
- **Because the link lives on the job row itself** (`linkedJobJarId`), a Job Jar *subtask* — just
  its own row with a `parentId` — can carry its own independent link to its own separate Tracker
  job, with its own separate cost/vendor tracking, entirely apart from whatever its parent task
  is linked to.
- The reverse direction works the same way: a job sent *from* Job Jar with an `estimatedMinutes`
  or `scheduledDate` value arrives here with `predictedHours`/`scheduledDate` pre-filled on the
  create form — both ordinary, already-editable fields, so this needs no special-case UI the way
  Job Jar's own create form does. This only ever happens via **Send**, never **Link** (which just
  points two already-existing jobs at each other), and only at the moment of creation — later
  edits on either side aren't kept in sync.

## Backing up your data

Since everything lives in this app's local database and private photo storage, uninstalling
the app or losing the device loses your data. Android's automatic backup
(`android:allowBackup="true"`, already set) will back up app data to the user's Google account
on supported devices, but that's not guaranteed for large photo storage and isn't a substitute
for something you've verified. There's no in-app export yet — worth adding if this becomes
something you rely on long-term.

## Tests

```bash
cd android
./gradlew test
```

Unit tests use hand-written fakes for `JobRepository` and the Room DAOs (rather than
Robolectric/instrumentation), so they run as plain JVM tests — no emulator, no Android SDK at
test-run time. `PhotoStorage` itself (real file I/O against `Context.filesDir`) is mocked with
MockK in repository tests rather than faked by hand, since it's a thin platform adapter with no
logic worth re-implementing in a fake. Covered:

- `JobRepositoryImplTest` — create/update (preserves `createdAt`)/delete a job, delete cascades
  to photo file cleanup, notes round-trip with their photos, adding/removing a photo from an
  existing note, status filtering, payment method create/update round-trips.
- `JobListViewModelTest` — status filtering, Active/Completed/All tab grouping, delete
  delegation.
- `JobFormViewModelTest` — validation blocks save on bad input, valid input creates a job and
  fires the callback, editing pre-populates the form from the repository, opening the form from
  a Job Jar deep link pre-fills title/category/`linkedJobJarId`, `save()` correctly signals
  whether the save was a fresh create vs. an edit (what the screen uses to decide whether to
  fire the `linked` return callback).
- `StatsViewModelTest` — the paid/partial/unpaid grouping-by-method logic: sums split correctly
  by `PaymentStatus`, a job with no `actualCost` counts toward `jobCount` but no total, jobs with
  no payment method land in an "Unassigned" bucket that's omitted when it would be empty.
- `DateFormattingTest` — date/date-time display formatting, including that sub-minute
  precision never leaks into what's shown.

Note that Room's `onDelete = SET_NULL` foreign key behavior (deleting a payment method that's in
use) is enforced by SQLite itself and isn't something the hand-written `FakeJobDao`/
`FakePaymentMethodDao` model — it's covered by manual review and by trying it in a real build,
not by a JVM unit test.
