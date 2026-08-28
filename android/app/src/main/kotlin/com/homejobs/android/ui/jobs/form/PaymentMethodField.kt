package com.homejobs.android.ui.jobs.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.homejobs.android.domain.model.PaymentMethod
import com.homejobs.android.ui.common.PaymentMethodEditorDialog

/**
 * Payment method picker for the job form — like EnumDropdown, but the options come from the
 * user's own managed list rather than an enum, the selection is nullable ("None"), and it ends
 * with an "add new" entry so a card/account can be created right from here instead of needing
 * to leave the form first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodField(
    methods: List<PaymentMethod>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit,
    onAddNew: (name: String, maxCredit: Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val selectedLabel = methods.firstOrNull { it.id == selectedId }?.name ?: "None"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Payment method") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onSelected(null)
                    expanded = false
                },
            )
            methods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method.name) },
                    onClick = {
                        onSelected(method.id)
                        expanded = false
                    },
                )
            }
            DropdownMenuItem(
                text = { Text("+ Add new payment method") },
                onClick = {
                    expanded = false
                    showAddDialog = true
                },
            )
        }
    }

    if (showAddDialog) {
        PaymentMethodEditorDialog(
            title = "Add payment method",
            onDismiss = { showAddDialog = false },
            onSave = { name, maxCredit ->
                onAddNew(name, maxCredit)
                showAddDialog = false
            },
        )
    }
}
