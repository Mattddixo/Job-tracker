package com.homejobs.android.parsing

import com.homejobs.android.domain.parsing.QuoteTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuoteTextParserTest {

    @Test
    fun `parses vendor name, email contact, and total from a well-formed quote`() {
        val text = """
            ABC Plumbing & Heating
            123 Main St, Springfield
            (555) 123-4567 - billing@abcplumbing.com

            QUOTE #4471

            Item                Qty   Price
            Water heater         1   $850.00
            Labor                4   $60.00

            Grand Total: $1,090.00
        """.trimIndent()

        val result = QuoteTextParser.parse(text)

        assertEquals("ABC Plumbing & Heating", result.vendorName)
        assertEquals("billing@abcplumbing.com", result.vendorContact)
        assertEquals(1090.00, result.quotedCost)
    }

    @Test
    fun `falls back to a phone number when there's no email`() {
        val text = """
            Sunrise Roofing
            Call us: (555) 987-6543

            Total: $2,300.00
        """.trimIndent()

        val result = QuoteTextParser.parse(text)

        assertEquals("(555) 987-6543", result.vendorContact)
    }

    @Test
    fun `skips a bare document-type heading when picking the vendor name`() {
        val text = """
            ESTIMATE

            Sunrise Roofing

            Total: $500.00
        """.trimIndent()

        val result = QuoteTextParser.parse(text)

        assertEquals("Sunrise Roofing", result.vendorName)
    }

    @Test
    fun `prefers a specific grand total over an earlier generic subtotal`() {
        val text = """
            Sunrise Roofing

            Subtotal: $940.00
            Tax: $75.60
            Grand Total: $1,015.60
        """.trimIndent()

        val result = QuoteTextParser.parse(text)

        assertEquals(1015.60, result.quotedCost)
    }

    @Test
    fun `does not guess a total from a bare line-item price with no total-style label`() {
        val text = """
            Sunrise Roofing

            Water heater $850.00
            Labor $240.00
        """.trimIndent()

        val result = QuoteTextParser.parse(text)

        assertNull(result.quotedCost)
    }

    @Test
    fun `returns every field null when nothing in the text matches`() {
        // No line here qualifies as a vendor name (both start with a digit, ruling out invoice
        // and PO numbers), and there's no email, phone, or total-style label anywhere.
        val text = """
            12345 Invoice Reference
            67890 Purchase Order
        """.trimIndent()

        val result = QuoteTextParser.parse(text)

        assertNull(result.vendorName)
        assertNull(result.vendorContact)
        assertNull(result.quotedCost)
    }
}
