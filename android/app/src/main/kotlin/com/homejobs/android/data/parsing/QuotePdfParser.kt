package com.homejobs.android.data.parsing

import android.content.Context
import android.net.Uri
import com.homejobs.android.domain.model.ParsedQuote
import com.homejobs.android.domain.parsing.QuoteTextParser
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Extracts the text layer from a picked PDF and hands it to [QuoteTextParser]. Only handles
 * PDFs that already carry real text (i.e. not a scanned image wrapped in a PDF) — a scanned quote
 * comes back as [ParsedQuote] with every field null, same as any other PDF nothing was found in,
 * rather than a special error. Adding an OCR fallback for scanned quotes is a separate,
 * not-yet-built follow-up (see the README).
 */
class QuotePdfParser @Inject constructor(@ApplicationContext private val context: Context) {

    fun parse(uri: Uri): ParsedQuote {
        val text = extractText(uri) ?: return ParsedQuote()
        return QuoteTextParser.parse(text)
    }

    private fun extractText(uri: Uri): String? {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        return input.use { stream ->
            PDDocument.load(stream).use { document -> PDFTextStripper().getText(document) }
        }
    }
}
