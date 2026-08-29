package com.homejobs.android.domain.parsing

import com.homejobs.android.domain.model.ParsedQuote

/**
 * Pulls a conservative subset of fields out of a PDF quote's already-extracted text: vendor name,
 * a contact (email or phone), and a total cost. Every heuristic here is written to fail closed —
 * return null — rather than guess, per the job form's "only fill what it actually finds" contract.
 * No line-item table parsing, no OCR, no reading of scanned/image-only PDFs — this only ever sees
 * a text layer that's already been extracted (see [com.homejobs.android.data.parsing.QuotePdfParser]).
 *
 * Deliberately pure Kotlin (no Android/PDFBox types) so the heuristics are unit-testable as plain
 * string-in, data-out logic, independent of PDF extraction itself.
 */
object QuoteTextParser {

    private val EMAIL_REGEX = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val PHONE_REGEX = Regex("(\\+?1[\\s.-]?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}")

    /**
     * Checked in this order — most specific/unambiguous label first — so a "Grand Total" or
     * "Balance Due" further down the page wins over an earlier "Subtotal" or per-item "Total"
     * line, rather than just grabbing whichever total-shaped number appears first.
     */
    private val TOTAL_LABEL_PRIORITY = listOf(
        "grand total",
        "total due",
        "amount due",
        "balance due",
        "total estimate",
        "estimated total",
        "quote total",
        "quoted total",
        "total cost",
        "total",
    )

    /** A label followed, within a short gap, by a dollar amount that includes cents. */
    private fun totalRegexFor(label: String) =
        Regex("(?i)" + Regex.escape(label) + "[^\\d$]{0,20}\\$?\\s?([\\d,]+\\.\\d{2})")

    private val DOCUMENT_HEADER_WORDS = setOf("quote", "quotation", "estimate", "invoice", "proposal")

    fun parse(text: String): ParsedQuote = ParsedQuote(
        vendorName = findVendorName(text),
        vendorContact = findContact(text),
        quotedCost = findTotalCost(text),
    )

    /**
     * Letterhead is almost always one of the first few non-blank lines. Skips lines that are
     * clearly something else (an email, a phone number, an address starting with a street
     * number, or a bare document-type heading like "ESTIMATE") rather than assuming line one is
     * always right.
     */
    private fun findVendorName(text: String): String? =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(6)
            .firstOrNull { line ->
                line.length in 2..60 &&
                    line.lowercase() !in DOCUMENT_HEADER_WORDS &&
                    !EMAIL_REGEX.containsMatchIn(line) &&
                    !PHONE_REGEX.containsMatchIn(line) &&
                    !line.first().isDigit()
            }

    /** Prefers an email (unambiguous) over a phone number (more prone to false positives). */
    private fun findContact(text: String): String? =
        EMAIL_REGEX.find(text)?.value ?: PHONE_REGEX.find(text)?.value

    private fun findTotalCost(text: String): Double? {
        for (label in TOTAL_LABEL_PRIORITY) {
            val amount = totalRegexFor(label).find(text)?.groupValues?.get(1)
            if (amount != null) return amount.replace(",", "").toDoubleOrNull()
        }
        return null
    }
}
