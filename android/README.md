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
- [pdfbox-android](https://github.com/TomRoush/PdfBox-Android) for on-device text extraction from
  a picked PDF quote — no network call, same offline-only stance as everything else here

## Project layout

```
app/src/main/kotlin/com/homejobs/android/
  domain/            pure Kotlin models + the JobRepository interface — no Room/Android types
    parsing/           QuoteTextParser — pure heuristics over already-extracted PDF text
  data/
    local/db/         Room entities, DAOs, database (jobs, job_notes, photos)
    local/photo/       PhotoStorage — copies camera/gallery images into app-private storage
    local/prefs/       ThemePreferences — persisted theme mode + color palette choice
    parsing/           QuotePdfParser — Android/PDFBox glue that feeds QuoteTextParser
    repository/       JobRepositoryImpl (Room-backed) + Entity/domain mappers
  di/                 Hilt modules (database, repository)
  ui/
    jobs/list/        job list screen + ViewModel (Active/Completed/All tabs, sort)
    jobs/detail/       job detail + notes timeline + photo capture/viewer + ViewModel
    jobs/form/         sectioned create/edit form + ViewModel + PDF-quote import
    jobs/photos/       all-photos grid for a job + ViewModel
    stats/             cost-by-payment-method stats + manage payment methods + ViewModel
    appearance/        Light/Dark/Custom toggle + color wheel picker screen
    navigation/        single-activity NavHost + routes
    theme/             custom Material 3 theme — Light/Dark schemes, Custom color derivation, typography
    common/            shared loading/empty/error/photo-viewer composables, date formatting, EnumDropdown
app/src/test/kotlin/com/homejobs/android/
  fakes/              hand-written fakes for JobRepository and the Room DAOs
  viewmodel/          ViewModel unit tests
  repository/         JobRepositoryImpl unit tests
  parsing/            QuoteTextParser unit tests
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
- **A PDF quote can pre-fill the job form, but only the fields it's actually confident about.**
  A PDF icon in `JobFormScreen`'s top bar opens Android's document picker (`ACTION_OPEN_DOCUMENT`,
  filtered to `application/pdf` — a system intent, no storage permission needed); the picked file
  is read straight off its `content://` Uri and never copied to disk, since it's only needed for
  the few seconds it takes to parse. `QuotePdfParser` (`data/parsing/`) extracts the PDF's text
  layer with pdfbox-android and hands it to `QuoteTextParser` (`domain/parsing/`, pure Kotlin —
  no Android or PDFBox types, so its regex heuristics are unit-tested as plain string-in,
  data-out logic). Only three fields are ever touched — **vendor name**, **vendor contact**
  (email preferred, phone as a fallback), and **quoted cost** — and each one comes back `null`
  unless a pattern matched with real confidence: the cost heuristic requires a dollar amount
  with cents sitting next to a total-style label (checking specific labels like "Grand Total" /
  "Balance Due" before falling back to a bare "Total", so a later grand total wins over an
  earlier subtotal) rather than grabbing the first dollar figure on the page, and the vendor
  name skips lines that are clearly a phone/email/address/invoice-number or a bare heading like
  "ESTIMATE". A field the parser doesn't find is left exactly as it was — nothing is cleared,
  nothing is invented — and the import result ("Imported from PDF: vendor, quoted cost." or
  "Couldn't find a vendor, contact, or total…") is shown back so a still-blank field isn't
  mistaken for one that was checked and found empty. Every imported value lands in the same
  editable text field a hand-typed one would, so it's reviewed (and correctable) before Save is
  ever pressed — nothing from a PDF is written to the database directly. A scanned/image-only
  PDF (no text layer) comes back with all three fields null, same as any PDF nothing was found
  in; adding an OCR fallback for that case is a separate, not-yet-built follow-up.

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
permission — there's nothing to talk to. The camera and gallery pickers, and the PDF-quote
picker, are launched via system intents (`ACTION_IMAGE_CAPTURE`, the Android Photo Picker,
`ACTION_OPEN_DOCUMENT`), so none of them needs a runtime permission request from this app.

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
  fires the callback, editing pre-populates the form from the repository, importing a PDF fills
  only the fields a (mocked) parser actually found and leaves the rest of the form untouched,
  importing a PDF with nothing recognizable leaves every field as it was.
- `StatsViewModelTest` — the paid/partial/unpaid grouping-by-method logic: sums split correctly
  by `PaymentStatus`, a job with no `actualCost` counts toward `jobCount` but no total, jobs with
  no payment method land in an "Unassigned" bucket that's omitted when it would be empty.
- `QuoteTextParserTest` — the actual regex heuristics behind PDF import: vendor name/contact/cost
  extraction from a realistic quote layout, a specific "Grand Total" winning over an earlier
  generic "Subtotal", a bare line-item price with no total-style label correctly *not* being
  treated as the total, and a text blob with no recognizable structure coming back with every
  field null instead of a wrong guess.
- `DateFormattingTest` — date/date-time display formatting, including that sub-minute
  precision never leaks into what's shown.

Note that Room's `onDelete = SET_NULL` foreign key behavior (deleting a payment method that's in
use) is enforced by SQLite itself and isn't something the hand-written `FakeJobDao`/
`FakePaymentMethodDao` model — it's covered by manual review and by trying it in a real build,
not by a JVM unit test.
