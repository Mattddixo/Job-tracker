package com.homejobs.android.data.parsing

import android.content.Context
import android.net.Uri
import com.homejobs.android.domain.model.ParsedQuote
import com.homejobs.android.domain.parsing.QuoteTextParser
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

/**
 * The outcome of trying to import a PDF quote. [hasTextLayer] is `false` for a PDF that's really
 * just a scanned/photographed page (or a "print to PDF" of a rendered image) with no real text
 * behind it — worth telling the user directly, since silently returning an all-null [ParsedQuote]
 * would look identical to "this had text but nothing matched" and read as a parsing failure
 * rather than "this PDF has no text at all, which needs OCR — a separate, not-yet-built feature."
 */
data class QuotePdfImportResult(
    val quote: ParsedQuote,
    val hasTextLayer: Boolean,
)

/**
 * Extracts the text layer from a picked PDF and hands it to [QuoteTextParser]. Only handles
 * PDFs that already carry real text — a scanned quote comes back with [QuotePdfImportResult.hasTextLayer]
 * false rather than a special error, so the caller can tell "nothing to read" apart from "read it,
 * found nothing." Adding an OCR fallback for scanned quotes is a separate, not-yet-built
 * follow-up (see the README).
 */
class QuotePdfParser @Inject constructor(@ApplicationContext private val context: Context) {

    fun parse(uri: Uri): QuotePdfImportResult {
        val text = extractText(uri)
        val hasTextLayer = text.trim().length >= MIN_TEXT_LENGTH
        val quote = if (hasTextLayer) QuoteTextParser.parse(text) else ParsedQuote()
        return QuotePdfImportResult(quote, hasTextLayer)
    }

    private fun extractText(uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri) ?: throw IOException("Could not open $uri")
        return input.use { stream ->
            PDDocument.load(stream).use { document -> PDFTextStripper().getText(document) }
        }
    }

    private companion object {
        /** Below this, treat it the same as "no text layer" rather than trying to parse noise. */
        const val MIN_TEXT_LENGTH = 20
    }
}
