#!/usr/bin/env bash
# Seeds a handful of demo jobs (and a couple of timeline notes) into a running
# Home Jobs Tracker API instance. Useful for kicking the tires after first
# `docker compose up`, or for populating a demo/dev environment.
#
# Usage:
#   BASE_URL=http://localhost:8080 API_KEY=... ./scripts/seed.sh

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
API_KEY="${API_KEY:?Set API_KEY to the server's configured API_KEY}"

post_job() {
  curl -sS -X POST "$BASE_URL/api/v1/jobs" \
    -H "Authorization: Bearer $API_KEY" \
    -H "Content-Type: application/json" \
    -d "$1"
}

post_note() {
  local job_id="$1"
  local body="$2"
  curl -sS -X POST "$BASE_URL/api/v1/jobs/$job_id/notes" \
    -H "Authorization: Bearer $API_KEY" \
    -H "Content-Type: application/json" \
    -d "{\"body\": \"$body\"}" > /dev/null
}

echo "Seeding demo jobs against $BASE_URL ..."

water_heater=$(post_job '{
  "title": "Replace water heater",
  "category": "Plumbing",
  "location": "Basement",
  "vendorName": "Acme Plumbing",
  "vendorContact": "555-0100",
  "status": "DONE",
  "quotedCost": 1200.00,
  "actualCost": 1350.00,
  "predictedHours": 4,
  "actualHours": 5.5,
  "scheduledDate": "2026-02-10",
  "completedDate": "2026-02-10",
  "warrantyExpiry": "2032-02-10",
  "paymentStatus": "PAID",
  "paymentMethod": "Credit card"
}')
water_heater_id=$(echo "$water_heater" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
post_note "$water_heater_id" "Acme confirmed the appointment for Feb 10."
post_note "$water_heater_id" "Install went smoothly, old unit hauled away."

fence=$(post_job '{
  "title": "Paint back fence",
  "category": "Exterior",
  "location": "Backyard",
  "status": "QUOTED",
  "quotedCost": 450.00,
  "predictedHours": 6,
  "paymentStatus": "UNPAID"
}')
fence_id=$(echo "$fence" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
post_note "$fence_id" "Got a quote from two contractors, going with the cheaper one."

gutter=$(post_job '{
  "title": "Fix leaking gutter",
  "category": "Repairs",
  "location": "Exterior - north side",
  "vendorName": "Dave the Handyman",
  "status": "SCHEDULED",
  "quotedCost": 180.00,
  "predictedHours": 2,
  "scheduledDate": "2026-03-01",
  "paymentStatus": "UNPAID"
}')

electrical=$(post_job '{
  "title": "Add outlet in garage",
  "category": "Electrical",
  "location": "Garage",
  "status": "IN_PROGRESS",
  "quotedCost": 300.00,
  "actualCost": 320.00,
  "predictedHours": 3,
  "actualHours": 3,
  "paymentStatus": "PARTIAL",
  "paymentMethod": "Check"
}')

echo "Done. Created jobs: water heater ($water_heater_id), fence ($fence_id)."
