package com.homejobs.android.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")

/** Formats an ISO-8601 date ("yyyy-MM-dd") for display, e.g. "Feb 10, 2026". */
fun String.toDisplayDate(): String = try {
    LocalDate.parse(this).format(dateFormatter)
} catch (e: DateTimeParseException) {
    this
}

/** Formats an epoch-millis timestamp for display in the device's local time zone, e.g. "Feb 10, 2026, 3:45 PM". */
fun Long.toDisplayDateTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(dateTimeFormatter)
