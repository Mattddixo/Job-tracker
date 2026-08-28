package com.homejobs.android.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Add/edit form for a payment method: just a name and an optional max credit — presence of a
 * max credit is what makes something "a card" (see PaymentMethod), there's no separate flag.
 * Shared by the job form's "add new" flow and the Stats screen's manage list.
 */
@Composable
fun PaymentMethodEditorDialog(
    title: String,
    initialName: String = "",
    initialMaxCredit: Double? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, maxCredit: Double?) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var maxCreditText by remember { mutableStateOf(initialMaxCredit?.let { formatPlain(it) }.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = maxCreditText,
                    onValueChange = { text -> if (text.matches(Regex("^\\d*\\.?\\d*$"))) maxCreditText = text },
                    label = { Text("Max credit (optional, for cards)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), maxCreditText.toDoubleOrNull()) },
                enabled = name.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun formatPlain(value: Double): String =
    if (value == Math.floor(value) && !value.isInfinite()) value.toLong().toString() else value.toString()
