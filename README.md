# Home Jobs Tracker

A standalone Android app for tracking household jobs/projects — repairs, renovations,
contractor work — with quoted-vs-actual cost and time comparison, a photo-backed notes
timeline, and Active/Completed/All views so you always know what's open and what's next.

Everything lives on-device (Room). There's no server, no account, no network permission —
see [`android/README.md`](./android) for the architecture and setup instructions.

## Why no backend

This started as a client/server app (Kotlin backend + Android client) for a homelab
deployment. In practice the actual use case — one person tracking their own jobs — never
needed multi-device or multi-user access, so the server was pure operational overhead: a
Docker host to keep running, a Tailscale network to maintain, secrets to manage, backend
code to redeploy on every change. None of that buys anything a single-user app doesn't
already get for free from Room. The backend was retired; this repo is Android-only now (its
history is still in git if a server ever becomes worth reviving).

## Data model

- **Job**: title, category, room/location, vendor + contact, status (`quoted → scheduled →
  in_progress → done → cancelled`), quoted/actual cost, predicted/actual hours, scheduled/
  completed/warranty dates, payment status + method. `costVariance` and `timeVariance`
  (`actual − quoted`) are always computed, never stored.
- **JobNote**: a timestamped timeline entry on a job, with zero or more attached photos;
  deleted along with the job.
- **Photo**: an image attached to a note (taken with the camera or picked from the gallery),
  copied into the app's private storage — see the Android README for why.

## License

No license file is included — add one if you plan to share this beyond your own household.
