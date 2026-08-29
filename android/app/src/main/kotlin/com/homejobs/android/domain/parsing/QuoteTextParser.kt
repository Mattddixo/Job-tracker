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
        "total amount due",
        "total due",
        "amount due",
        "balance due",
        "invoice total",
        "total invoice",
        "total estimate",
        "estimated total",
        "quote total",
        "quoted total",
        "amount payable",
        "total amount",
        "total cost",
        "total",
    )

    /**
     * A label followed, within a short gap, by a dollar amount. Cents are only optional when a
     * `$` is actually present — that's the signal that distinguishes "Total: $450" (a real whole-
     * dollar total) from "Total items: 5" (a bare count that happens to follow a total-ish word).
     * Without a `$`, cents are required, same reasoning as before.
     */
    private fun totalRegexFor(label: String) = Regex(
        "(?i)" + Regex.escape(label) + "[^\\d$]{0,20}" +
            "(?:\\$\\s?([\\d,]+(?:\\.\\d{2})?)|([\\d,]+\\.\\d{2}))",
    )

    /**
     * A line that's clearly a document-type heading or an invoice/PO reference rather than a
     * business name — "Invoice", "Invoice #4471", "Invoice Data", "Tax Invoice", "Quote Date: …"
     * all match, not just a line that's *exactly* one of these words on its own.
     */
    private val DOCUMENT_HEADER_LINE = Regex(
        "(?i)^\\s*(tax\\s+)?(invoice|quote|quotation|estimate|proposal|statement|receipt|bill)\\b.*$",
    )

    fun parse(text: String): ParsedQuote = ParsedQuote(
        vendorName = findVendorName(text),
        vendorContact = findContact(text),
        quotedCost = findTotalCost(text),
    )

    /**
     * Letterhead is almost always one of the first few non-blank lines. Skips lines that are
     * clearly something else: an email, a phone number, an address or invoice/PO number starting
     * with a digit, a line that's mostly digits (a reference number or date), or a document-type
     * heading like "Tax Invoice" or "Invoice #4471" — rather than assuming line one is always
     * the company name.
     */
    private fun findVendorName(text: String): String? =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(8)
            .firstOrNull(::isPlausibleVendorLine)

    private fun isPlausibleVendorLine(line: String): Boolean {
        if (line.length !in 2..60) return false
        if (line.first().isDigit()) return false
        if (DOCUMENT_HEADER_LINE.matches(line)) return false
        if (EMAIL_REGEX.containsMatchIn(line)) return false
        if (PHONE_REGEX.containsMatchIn(line)) return false
        val digitCount = line.count { it.isDigit() }
        return digitCount <= line.length / 3
    }

    /** Prefers an email (unambiguous) over a phone number (more prone to false positives). */
    private fun findContact(text: String): String? =
        EMAIL_REGEX.find(text)?.value ?: PHONE_REGEX.find(text)?.value

    private fun findTotalCost(text: String): Double? {
        for (label in TOTAL_LABEL_PRIORITY) {
            val match = totalRegexFor(label).find(text) ?: continue
            val amount = match.groupValues[1].ifBlank { match.groupValues[2] }
            return amount.replace(",", "").toDoubleOrNull()
        }
        return null
    }
}
