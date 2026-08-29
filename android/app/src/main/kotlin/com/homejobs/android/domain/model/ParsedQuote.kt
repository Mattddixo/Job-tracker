package com.homejobs.android.domain.model

/**
 * Fields pulled out of a PDF quote's text, one per job-form field we're confident enough to
 * pre-fill automatically. Every field is nullable and stays null unless a pattern matched with
 * real confidence — this is deliberately not a "best guess" for every field on the form; see
 * [com.homejobs.android.domain.parsing.QuoteTextParser].
 */
data class ParsedQuote(
    val vendorName: String? = null,
    val vendorContact: String? = null,
    val quotedCost: Double? = null,
)
